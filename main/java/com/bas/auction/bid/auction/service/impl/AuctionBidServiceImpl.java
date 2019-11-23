package com.bas.auction.bid.auction.service.impl;

import com.bas.auction.bid.auction.service.AuctionBidService;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.neg.dao.NegotiationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

@Service
public class AuctionBidServiceImpl implements AuctionBidService {
    private final Logger logger = LoggerFactory.getLogger(AuctionBidServiceImpl.class);
    private final BigDecimal MIN_STEP = new BigDecimal("0.01");
    private final BigDecimal HUNDRED = new BigDecimal(100);
    private final BidDAO bidDAO;
    private final NegotiationDAO negDAO;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public AuctionBidServiceImpl(BidDAO bidDAO, NegotiationDAO negDAO, BidLineDAO bidLineDAO, ExchangeRateService exchangeRateService) {
        this.bidDAO = bidDAO;
        this.negDAO = negDAO;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public Map<Integer, BigDecimal> calculateAuctionPrices(Long bidId) {
        Long negId = bidDAO.findBidNegId(bidId);
        String bidCurrency = bidDAO.findBidCurrency(bidId);
        BigDecimal bidPriceStep = negDAO.findAuctionBidStep(negId);
        String bidStepType = negDAO.findAuctionBidStepType(negId);
        BigDecimal exchangeRate = exchangeRateService.findCurrentExchangeRate(bidCurrency);
        logger.debug("calc auc prices: bidId={}, stepType={}, step={}", bidId, bidStepType, bidPriceStep);
        Map<Integer, BigDecimal> bidCurrentBestPrices = bidDAO.findAuctionBidCurrentBestPrices(negId);
        return bidCurrentBestPrices.entrySet().stream()
                .collect(toMap(Entry::getKey,
                        e -> calculateNextBestPrice(e.getValue(), bidPriceStep, bidStepType, exchangeRate)));
    }

    @Override
    public BigDecimal calculateAuctionPrice(Long bidId, Integer lineNum) {
        Long negId = bidDAO.findBidNegId(bidId);
        String bidCurrency = bidDAO.findBidCurrency(bidId);
        String bidStepType = negDAO.findAuctionBidStepType(negId);
        BigDecimal bidPriceStep = negDAO.findAuctionBidStep(negId);
        BigDecimal exchangeRate = exchangeRateService.findCurrentExchangeRate(bidCurrency);
        logger.debug("calc auc line price: bidId={}, lineNum={}, stepType={}, step={}", bidId, lineNum, bidStepType, bidPriceStep);
        BigDecimal price = bidDAO.findAuctionBidLineCurrentBestPrice(negId, lineNum);
        return calculateNextBestPrice(price, bidPriceStep, bidStepType, exchangeRate);
    }

    private BigDecimal calculateNextBestPrice(BigDecimal price, BigDecimal bidPriceStep,
                                              String bidStepType, BigDecimal exchangeRate) {
        BigDecimal nextBestPrice;
        if ("PERCENT".equals(bidStepType)) {
            nextBestPrice = calculateNextBestPriceByPercent(price, bidPriceStep);
        } else {
            nextBestPrice = calculateNextBestPriceBySum(price, bidPriceStep);
        }
        return convertPrice(price, nextBestPrice, exchangeRate);
    }

    private BigDecimal convertPrice(BigDecimal price, BigDecimal bestPrice, BigDecimal exchangeRate) {
        price = price.divide(exchangeRate, 2, RoundingMode.DOWN);
        bestPrice = bestPrice.divide(exchangeRate, 2, RoundingMode.DOWN);
        if (price.subtract(bestPrice).compareTo(MIN_STEP) < 0)
            bestPrice = price.subtract(MIN_STEP);
        return bestPrice;
    }

    private BigDecimal calculateNextBestPriceByPercent(BigDecimal price, BigDecimal bidStep) {
        BigDecimal percent = bidStep.divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal coefficient = BigDecimal.ONE.subtract(percent);
        return price.multiply(coefficient).setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal calculateNextBestPriceBySum(BigDecimal price, BigDecimal bidStep) {
        return price.subtract(bidStep).setScale(2, RoundingMode.DOWN);
    }
}
