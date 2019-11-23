package com.bas.auction.salesroom.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.auction.service.AuctionBidService;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.core.Conf;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(path = "/salesroom", produces = APPLICATION_JSON_UTF8_VALUE)
public class SalesroomController extends RestControllerExceptionHandler {
    private final BidDAO bidDAO;
    private final AuctionBidService auctionBidService;
    private final Conf conf;


    @Autowired
    public SalesroomController(MessageDAO messageDAO, BidDAO bidDAO, AuctionBidService auctionBidService, Conf conf) {
        super(messageDAO);
        this.bidDAO = bidDAO;
        this.auctionBidService = auctionBidService;
        this.conf = conf;
    }

    @RequestMapping(path = "/details/{negId}", method = GET)
    public Map<String, Object> salesroomDetails(@PathVariable Long negId,
                                                @RequestParam("currency") String currencyCode,
                                                @CurrentUser User user) {
        MDC.put("action", "salesroom details");
        if(currencyCode == null)
            currencyCode = conf.getFunctionalCurrency();
        Map<String, Object> res = new HashMap<>();
        Map<Integer, BigDecimal> allSaleroomBids = bidDAO.findAllSaleroomBids(negId, currencyCode);
        Map<Integer, Map<String, BigDecimal>> supplierLastSaleroomBids = bidDAO.findSupplierLastSaleroomBids(negId, user.getSupplierId(), currencyCode);
        Map<Integer, Long> supplierRanks = bidDAO.findSupplierRanks(negId, user.getSupplierId());
        res.put("all_prices", allSaleroomBids);
        res.put("my_prices", supplierLastSaleroomBids);
        res.put("my_ranks", supplierRanks);
        return res;
    }

    @RequestMapping(path = "/nextbestprices/{bidId}", method = GET)
    public Map<Integer, BigDecimal> calculateAuctionPrices(@PathVariable Long bidId,
                                                           @CurrentUser User user) {
        MDC.put("action", "calc auc prices");
        return auctionBidService.calculateAuctionPrices(bidId);
    }

    @RequestMapping(path = "/nextbestprices/{bidId}/{lineNum}", method = GET)
    public BigDecimal calculateAuctionPrice(@PathVariable Long bidId,
                                            @PathVariable Integer lineNum,
                                            @CurrentUser User user) {
        MDC.put("action", "calc auc line price");
        return auctionBidService.calculateAuctionPrice(bidId, lineNum);
    }
}