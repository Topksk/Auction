package com.bas.auction.bid.permission.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.permission.dto.BidLinePermissionDetails;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.bid.permission.service.BidForeignCurrencyPermissionService;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.neg.setting.service.NegRequirementService;
import com.bas.auction.profile.customer.setting.dao.MdRequirementDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class BidForeignCurrencyPermissionServiceImpl implements BidForeignCurrencyPermissionService {
    private final ExchangeRateService exchangeRateService;
    private final MessageDAO messageDAO;
    private final BidPermissionsService bidPermsService;
    private final BidDAO bidDAO;
    private final NegRequirementService negRequirementService;

    @Autowired
    public BidForeignCurrencyPermissionServiceImpl(MdRequirementDAO negReqDAO, ExchangeRateService exchangeRateService,
                                                   BidDAO bidDAO, BidPermissionsService bidPermsService, MessageDAO messageDAO, NegRequirementService negRequirementService) {
        this.exchangeRateService = exchangeRateService;
        this.bidDAO = bidDAO;
        this.bidPermsService = bidPermsService;
        this.messageDAO = messageDAO;
        this.negRequirementService = negRequirementService;
    }

    @Override
    public void performNegBidsForeignCurrencyControl(User user, Long negId) {
        ForeignCurrencyPermissionDto dto = new ForeignCurrencyPermissionDto(negId);
        bidPermsService.permitNegBidsInFunctionalCurrency(negId, dto.requirementId);
        List<Entry<Long, String>> bidsForeignCurrencies = bidDAO.findNegBidsCurrencyCodes(negId);
        List<BidLinePermissions> allForeignCurrencyPermissions =
                bidsForeignCurrencies.stream()
                        .map(bidCurrency -> performBidForeignCurrencyControl(dto, bidCurrency))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
        bidPermsService.update(user, allForeignCurrencyPermissions);
    }

    private List<BidLinePermissions> performBidForeignCurrencyControl(ForeignCurrencyPermissionDto dto,
                                                                      Entry<Long, String> bidCurrency) {
        BigDecimal exchangeRate = findCurrencyExchangeRate(bidCurrency.getValue(), dto.currencyRatesCache);
        Long bidId = bidCurrency.getKey();
        bidDAO.updateBidUnlockExchangeRate(bidId, exchangeRate);
        return performBidForeignCurrencyControl(dto, bidId, exchangeRate);
    }

    private BigDecimal findCurrencyExchangeRate(String currency, Map<String, BigDecimal> cachedCurrencyRates) {
        if (!cachedCurrencyRates.containsKey(currency)) {
            BigDecimal rate = exchangeRateService.findCurrentExchangeRate(currency);
            cachedCurrencyRates.put(currency, rate);
        }
        return cachedCurrencyRates.get(currency);
    }

    private List<BidLinePermissions> performBidForeignCurrencyControl(ForeignCurrencyPermissionDto dto, Long bidId,
                                                                      BigDecimal exchangeRate) {
        List<Map<String, Object>> negAndBidLinesPrices = bidDAO.findNegAndBidLinesPrices(bidId);
        return negAndBidLinesPrices.stream()
                .map(negAndBidLinePrices -> performBidLineForeignCurrencyControl(dto, bidId, exchangeRate, negAndBidLinePrices))
                .collect(Collectors.toList());
    }

    private BidLinePermissions performBidLineForeignCurrencyControl(ForeignCurrencyPermissionDto dto, Long bidId,
                                                                    BigDecimal rate, Map<String, ?> negAndBidLineprices) {
        Integer bidLineNum = (Integer) negAndBidLineprices.get("line_num");
        boolean permit = isPermittedBidLinePrice(rate, negAndBidLineprices);
        String reason = permit ? null : dto.rejectReason;
        BidLinePermissionDetails linePermission = new BidLinePermissionDetails();
        linePermission.setRequirementId(dto.requirementId);
        linePermission.setPermitted(permit);
        linePermission.setRejectReason(reason);
        BidLinePermissions permission = new BidLinePermissions();
        permission.setBidId(bidId);
        permission.setBidLineNum(bidLineNum);
        permission.setPermissions(Collections.singletonList(linePermission));
        return permission;
    }

    private boolean isPermittedBidLinePrice(BigDecimal rate, Map<String, ?> price) {
        BigDecimal bidPrice = (BigDecimal) price.get("bid_price");
        BigDecimal negLineUnitPrice = (BigDecimal) price.get("unit_price");
        BigDecimal convertedUnitPrice = negLineUnitPrice.divide(rate, 2, RoundingMode.DOWN);
        return bidPrice.compareTo(convertedUnitPrice) <= 0;
    }

    private class ForeignCurrencyPermissionDto {
        final Long requirementId;
        final String rejectReason;
        final Map<String, BigDecimal> currencyRatesCache = new HashMap<>();

        ForeignCurrencyPermissionDto(Long negId) {
            this.requirementId = negRequirementService.findForeignCurrencyControlRequirementId(negId);
            this.rejectReason = messageDAO.getFromDb("FOREIGN_CURRENCY_CONTROL_REQ", "RU");
        }
    }
}
