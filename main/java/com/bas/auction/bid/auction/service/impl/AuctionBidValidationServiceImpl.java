package com.bas.auction.bid.auction.service.impl;

import com.bas.auction.bid.auction.service.AuctionBidService;
import com.bas.auction.bid.auction.service.AuctionBidValidationService;
import com.bas.auction.bid.auction.service.PriceDiffLessThanAuctionStep;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

@Service
public class AuctionBidValidationServiceImpl implements AuctionBidValidationService {
    private final Logger logger = LoggerFactory.getLogger(AuctionBidValidationServiceImpl.class);
    private final AuctionBidService auctionBidService;
    private final BidDAO bidDAO;
    private final BidLineDAO bidLineDAO;

    @Autowired
    public AuctionBidValidationServiceImpl(AuctionBidService auctionBidService, BidDAO bidDAO, BidLineDAO bidLineDAO) {
        this.auctionBidService = auctionBidService;
        this.bidDAO = bidDAO;
        this.bidLineDAO = bidLineDAO;
    }

    @Override
    public void validateBidPrice(Long bidId) {
        logger.debug("validate auction bid price: bidId={}", bidId);
        Map<Integer, BigDecimal> auctionNextBestPrices = auctionBidService.calculateAuctionPrices(bidId);
        Map<Integer, BigDecimal> bidLinePrices = bidLineDAO.findBidLinePrices(bidId)
                .entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(toMap(Entry::getKey, Entry::getValue));
        boolean allBidPricesAreBest = bidLinePrices.entrySet().stream()
                .allMatch(e -> bidPriceDiffLessThanBidStep(e, auctionNextBestPrices));
        if (allBidPricesAreBest)
            return;
        validateAtLeastOnePriceIsTheBest(bidLinePrices, auctionNextBestPrices);
        validateReplacedBidPrices(bidId, bidLinePrices, auctionNextBestPrices);
    }

    protected void validateAtLeastOnePriceIsTheBest(Map<Integer, BigDecimal> bidLinePrices, Map<Integer, BigDecimal> auctionNextBestPrices) {
        boolean atLeastOnePriceIsTheBest = bidLinePrices.entrySet().stream()
                .anyMatch(e -> bidPriceDiffLessThanBidStep(e, auctionNextBestPrices));
        if (!atLeastOnePriceIsTheBest)
            throw new PriceDiffLessThanAuctionStep();
    }

    protected void validateReplacedBidPrices(Long bidId,
                                             Map<Integer, BigDecimal> bidLinePrices,
                                             Map<Integer, BigDecimal> auctionNextBestPrices) {
        Long replacedBidId = bidDAO.findReplacedBidId(bidId);
        if (replacedBidId == null)
            throw new PriceDiffLessThanAuctionStep();
        bidLinePrices = bidLinePrices.entrySet().stream()
                .filter(e -> !bidPriceDiffLessThanBidStep(e, auctionNextBestPrices))
                .collect(toMap(Entry::getKey, Entry::getValue));
        Map<Integer, BigDecimal> replacedBidLinePrices = bidLineDAO.findBidLinePrices(replacedBidId)
                .entrySet().stream()
                .filter(e -> e.getValue() != null)
                .collect(toMap(Entry::getKey, Entry::getValue));
        boolean bidPricesIsEqualToReplacedBidPrices = bidLinePrices.entrySet().stream()
                .allMatch(e -> bidPriceIsEqualToReplacedBidPrice(e, replacedBidLinePrices));
        if (!bidPricesIsEqualToReplacedBidPrices)
            throw new PriceDiffLessThanAuctionStep();
    }

    protected boolean bidPriceDiffLessThanBidStep(Entry<Integer, BigDecimal> bidLinePrice,
                                                  Map<Integer, BigDecimal> auctionNextBestPrices) {
        BigDecimal bidLineCurrentBestPrice = auctionNextBestPrices.get(bidLinePrice.getKey());
        return bidLinePrice.getValue().compareTo(bidLineCurrentBestPrice) <= 0;
    }

    protected boolean bidPriceIsEqualToReplacedBidPrice(Entry<Integer, BigDecimal> bidLinePrice,
                                                        Map<Integer, BigDecimal> replacedBidLinePrices) {
        BigDecimal replacedBidLinePrice = replacedBidLinePrices.get(bidLinePrice.getKey());
        return replacedBidLinePrice != null &&
                bidLinePrice.getValue().compareTo(replacedBidLinePrice) == 0;
    }
}
