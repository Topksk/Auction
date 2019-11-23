package com.bas.auction.neg.setting.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.setting.dao.NegSettingDAO;
import com.bas.auction.neg.setting.dto.NegSetting;
import com.bas.auction.neg.setting.service.NegDiscountService;
import com.bas.auction.neg.setting.service.NegRequirementService;
import com.bas.auction.neg.setting.service.NegSettingService;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegSettingServiceImpl implements NegSettingService {
    private final NegSettingDAO negSettingDAO;
    private final CustomerSettingDAO customerSettingDAO;
    private final NegRequirementService negRequirementService;
    private final NegDiscountService negDiscountService;

    @Autowired
    public NegSettingServiceImpl(NegSettingDAO negSettingDAO, CustomerSettingDAO customerSettingDAO, NegRequirementService negRequirementService, NegDiscountService negDiscountService) {
        this.negSettingDAO = negSettingDAO;
        this.customerSettingDAO = customerSettingDAO;
        this.negRequirementService = negRequirementService;
        this.negDiscountService = negDiscountService;
    }

    @Override
    @SpringTransactional
    public void create(Negotiation neg) {
        NegSetting negSetting = createNegSettings(neg);
        negSettingDAO.insert(negSetting);
        negRequirementService.create(neg);
        if (neg.getNegType() == NegType.TENDER || neg.getNegType() == NegType.TENDER2)
            negDiscountService.create(neg);
    }

    private NegSetting createNegSettings(Negotiation neg) {
        CustomerSetting customerSetting = customerSettingDAO.findByIdWithoutDetails(neg.getSettingId());
        NegSetting negSetting = new NegSetting();
        negSetting.setNegId(neg.getNegId());
        if (neg.getNegType() == NegType.AUCTION) {
            negSetting.setAwardMethod(customerSetting.getAuctionAwardMethod());
            negSetting.setForeignCurrencyControl(customerSetting.isAuctionForeignCurrencyControl());
            negSetting.setAuctionDuration(customerSetting.getAuctionDuration());
            negSetting.setAuctionExtDuration(customerSetting.getAuctionExtDuration());
            negSetting.setAuctionExtNumber(customerSetting.getAuctionExtNumber());
            negSetting.setAuctionExtTimeLeft(customerSetting.getAuctionExtTimeLeft());
        } else if (neg.getNegType() == NegType.RFQ) {
            negSetting.setAwardMethod(customerSetting.getRfqAwardMethod());
            negSetting.setForeignCurrencyControl(customerSetting.isRfqForeignCurrencyControl());
        } else if (neg.getNegType() == NegType.TENDER) {
            negSetting.setAwardMethod("SECRETARY");
            negSetting.setForeignCurrencyControl(customerSetting.isTenderForeignCurrencyControl());
        } else if (neg.getNegType() == NegType.TENDER2) {
            negSetting.setAwardMethod("SECRETARY");
            negSetting.setForeignCurrencyControl(customerSetting.isTender2ForeignCurrencyControl());
        }
        negSetting.setCreatedBy(neg.getCreatedBy());
        negSetting.setLastUpdatedBy(neg.getLastUpdatedBy());
        return negSetting;
    }

    @Override
    public void copyNegSettings(User user, Long sourceNegId, Long destinationNegId) {
        NegSetting negSetting = negSettingDAO.findNegSetting(sourceNegId);
        negSetting.setNegId(destinationNegId);
        negSetting.setCreatedBy(user.getUserId());
        negSetting.setLastUpdatedBy(user.getUserId());
        negSettingDAO.insert(negSetting);
        negRequirementService.copyNegRequirements(user, sourceNegId, destinationNegId);
        negDiscountService.copyNegDiscounts(user, sourceNegId, destinationNegId);
    }

    @Override
    @SpringTransactional
    public void delete(Long negId) {
        negDiscountService.delete(negId);
        negRequirementService.delete(negId);
        negSettingDAO.delete(negId);
    }

    @Override
    public String findNegAwardMethod(Long negId) {
        return negSettingDAO.findAwardMethod(negId);
    }

    @Override
    public Integer findAuctionDuration(Long negId) {
        return negSettingDAO.findAuctionDuration(negId);
    }

    @Override
    public boolean isNegForeignCurrencyControlEnabled(Long negId) {
        return negSettingDAO.isNegForeignCurrencyControlEnabled(negId);
    }
}
