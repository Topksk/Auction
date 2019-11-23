package com.bas.auction.bid.publish.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.auction.service.AuctionBidValidationService;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.bid.publish.service.*;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Objects.deepEquals;
import static java.util.stream.Collectors.*;

@Service
public class BidReportValidationServiceImpl implements BidReportValidationService {
    private final BigDecimal MINUS_ONE = new BigDecimal("-1");
    private final BidDAO bidDAO;
    private final NegotiationDAO negDAO;
    private final NegLineDAO negLineDAO;
    private final AuctionBidValidationService auctionBidValidationService;
    private final BidDiscountService bidDiscountService;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public BidReportValidationServiceImpl(BidDAO bidDAO, NegotiationDAO negDAO, NegLineDAO negLineDAO, AuctionBidValidationService auctionBidValidationService, BidDiscountService bidDiscountService, ExchangeRateService exchangeRateService) {
        this.bidDAO = bidDAO;
        this.negDAO = negDAO;
        this.negLineDAO = negLineDAO;
        this.auctionBidValidationService = auctionBidValidationService;
        this.bidDiscountService = bidDiscountService;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public void validateBidReport(User user, Long bidId) {
        Long negId = bidDAO.findBidNegId(bidId);
        Bid bid = bidDAO.findById(user, bidId);
        validateBidParticipationByPrice(bid);
        boolean isAuction = negDAO.findIsAuction(negId);
        if (isAuction)
            auctionBidValidationService.validateBidPrice(bidId);
        else
            validateBidPrice(bid);
        if (bid.getReplacedBidId() == null)
            return;
        validateReplacedBid(isAuction, bid, user);
    }

    @Override
    public void validateTender2Stage1BidParticipationAppl(User user, Long bidId) {
        Bid bid = bidDAO.findById(user, bidId);
        validateBidParticipation(bid);
    }

    protected void validateReplacedBid(boolean isAuction, Bid bid, User user) {
        Bid replacedBid = bidDAO.findById(user, bid.getReplacedBidId());
        if (isAuction && isAnyBidLineParticipationChanged(bid, replacedBid))
            throw new CantChangeBidLineParticipationForAuction();
        if (isBidHeaderChanged(bid, replacedBid))
            return;
        if (isAnyBidLineChanged(bid, replacedBid))
            return;
        if (isAnyBidFilesChanged(bid, replacedBid))
            return;
        if (isAnyBidDiscountChanged(bid.getBidId(), bid.getReplacedBidId()))
            return;
        throw new ReplacedBidMustBeDifferentFromOriginalBid();
    }

    protected void validateBidParticipationByPrice(Bid bid) {
        boolean isAnyParticipatedBidLineExists = bid.getBidLines().stream().anyMatch(this::isBidLineParticipates);
        if (!isAnyParticipatedBidLineExists)
            throw new AtLeastOneBidLinePriceRequired();
    }

    protected void validateBidParticipation(Bid bid) {
        boolean isAnyParticipatedBidLineExists = bid.getBidLines().stream()
                .map(BidLine::getParticipateTender2)
                .filter(Objects::nonNull)
                .anyMatch(p -> p);
        if (!isAnyParticipatedBidLineExists)
            throw new AtLeastOneParticipatedBidLineRequired();
    }

    protected void validateBidPrice(Bid bid) {
        Map<Integer, BigDecimal> negLineUnitPrices = negLineDAO.findNegLineUnitPrices(bid.getNegId());
        BigDecimal currentExchangeRate = exchangeRateService.findCurrentExchangeRate(bid.getCurrencyCode());
        boolean anyBidPriceGreaterThanUnitPrice = bid.getBidLines().stream()
                .filter(bl -> bl.getBidPrice() != null)
                .anyMatch(bl -> compareBidLinePriceWithNegLineUnitPrice(negLineUnitPrices, bl, currentExchangeRate));
        if (anyBidPriceGreaterThanUnitPrice)
            throw new BidPricesShouldNotBeGreaterThanNegUnitPriceException();
    }

    protected boolean compareBidLinePriceWithNegLineUnitPrice(Map<Integer, BigDecimal> negLineUnitPrices, BidLine bidLine, BigDecimal currentExchangeRate) {
        BigDecimal unitPrice = negLineUnitPrices.get(bidLine.getLineNum());
        unitPrice = unitPrice.divide(currentExchangeRate, 2, RoundingMode.DOWN);
        return bidLine.getBidPrice().compareTo(unitPrice) > 0;
    }

    protected boolean isAnyBidLineParticipationChanged(Bid bid, Bid replacedBid) {
        List<Integer> bidParticipatedLineNums = bid.getBidLines().stream()
                .filter(this::isBidLineParticipates)
                .map(BidLine::getLineNum)
                .collect(toList());
        List<Integer> replacedBidParticipatedLineNums = replacedBid.getBidLines().stream()
                .filter(this::isBidLineParticipates)
                .map(BidLine::getLineNum)
                .collect(toList());
        return !bidParticipatedLineNums.containsAll(replacedBidParticipatedLineNums);
    }

    protected boolean isBidLineParticipates(BidLine bidLine) {
        return bidLine.getBidPrice() != null && bidLine.getBidPrice().doubleValue() > 0;
    }

    protected boolean isBidHeaderChanged(Bid bid, Bid replacedBid) {
        return !deepEquals(bid.getBidComments(), replacedBid.getBidComments()) ||
                !deepEquals(bid.getBidLimitDays(), replacedBid.getBidLimitDays());
    }

    protected boolean isAnyBidLineChanged(Bid bid, Bid replacedBid) {
        Map<Integer, BigDecimal> bidLinesMap = bid.getBidLines().stream()
                .collect(toMap(BidLine::getLineNum, bl -> Optional.ofNullable(bl.getBidPrice()).orElse(BigDecimal.ZERO)));
        Map<Integer, BigDecimal> replacedBidLinesMap = replacedBid.getBidLines().stream()
                .collect(toMap(BidLine::getLineNum, bl -> Optional.ofNullable(bl.getBidPrice()).orElse(BigDecimal.ZERO)));
        return bidLinesMap.entrySet().stream()
                .anyMatch(entry -> !deepEquals(entry.getValue(), replacedBidLinesMap.get(entry.getKey())));
    }

    protected boolean isAnyBidFilesChanged(Bid bid, Bid replacedBid) {
        return isFileCountChanged(bid, replacedBid) ||
                isAnyDifferentFileNamesExists(bid, replacedBid) ||
                isAnyFilesWithDifferentContentExists(bid, replacedBid);
    }

    protected boolean isFileCountChanged(Bid bid, Bid replacedBid) {
        long bidFileCount = bid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .count();
        long replacedBidFileCount = replacedBid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .count();
        return bidFileCount != replacedBidFileCount;
    }

    protected boolean isAnyDifferentFileNamesExists(Bid bid, Bid replacedBid) {
        Set<String> bidFileNames = bid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .map(DocFile::getFileName)
                .collect(toSet());
        Set<String> replacedBidFileNames = replacedBid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .map(DocFile::getFileName)
                .collect(toSet());
        bidFileNames.removeAll(replacedBidFileNames);
        return !bidFileNames.isEmpty();
    }

    protected boolean isAnyFilesWithDifferentContentExists(Bid bid, Bid replacedBid) {
        Set<String> bidFileHashes = bid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .map(DocFile::getHashValue)
                .collect(toSet());
        Set<String> replacedBidFileHashes = replacedBid.getBidFiles().stream()
                .filter(f -> !f.getIsSystemGenerated())
                .map(DocFile::getHashValue)
                .collect(toSet());
        bidFileHashes.removeAll(replacedBidFileHashes);
        return !bidFileHashes.isEmpty();
    }

    protected boolean isAnyBidDiscountChanged(Long bidId, Long replacedBidId) {
        List<BidDiscount> bidDiscounts = bidDiscountService.findBidDiscounts(bidId);
        List<BidDiscount> replacedBidDiscounts = bidDiscountService.findBidDiscounts(replacedBidId);
        Map<Integer, List<BidDiscount>> bidLineDiscountsMap = bidDiscounts.stream()
                .collect(groupingBy(BidDiscount::getBidLineNum));
        Map<Integer, List<BidDiscount>> replacedBidLineDiscountsMap = replacedBidDiscounts.stream()
                .collect(groupingBy(BidDiscount::getBidLineNum));
        return bidLineDiscountsMap.entrySet().stream()
                .anyMatch(entry -> isAnyBidLineDiscountChanged(entry, replacedBidLineDiscountsMap));
    }

    protected boolean isAnyBidLineDiscountChanged(Entry<Integer, List<BidDiscount>> entry, Map<Integer, List<BidDiscount>> replacedBidLineDiscountsMap) {
        List<BidDiscount> replacedBidLineDiscounts = replacedBidLineDiscountsMap.get(entry.getKey());
        return isAnyBidLineDiscountBoolValueChanged(entry.getValue(), replacedBidLineDiscounts) ||
                isAnyBidLineDiscountNumberValueChanged(entry.getValue(), replacedBidLineDiscounts);
    }

    protected boolean isAnyBidLineDiscountBoolValueChanged(List<BidDiscount> bidDiscounts, List<BidDiscount> replacedBidDiscounts) {
        Map<Long, Boolean> bidLinesDiscountsMap = bidDiscounts.stream()
                .collect(toMap(BidDiscount::getDiscountId, bd -> Optional.ofNullable(bd.getBoolValue()).orElse(false)));
        Map<Long, Boolean> replacedBidLinesDiscountsMap = replacedBidDiscounts.stream()
                .collect(toMap(BidDiscount::getDiscountId, bd -> Optional.ofNullable(bd.getBoolValue()).orElse(false)));
        return bidLinesDiscountsMap.entrySet().stream()
                .anyMatch(entry -> !deepEquals(entry.getValue(), replacedBidLinesDiscountsMap.get(entry.getKey())));
    }

    protected boolean isAnyBidLineDiscountNumberValueChanged(List<BidDiscount> bidDiscounts, List<BidDiscount> replacedBidDiscounts) {
        Map<Long, BigDecimal> bidLinesDiscountsMap = bidDiscounts.stream()
                .collect(toMap(BidDiscount::getDiscountId, bd -> Optional.ofNullable(bd.getNumberValue()).orElse(MINUS_ONE)));
        Map<Long, BigDecimal> replacedBidLinesDiscountsMap = replacedBidDiscounts.stream()
                .collect(toMap(BidDiscount::getDiscountId, bd -> Optional.ofNullable(bd.getNumberValue()).orElse(MINUS_ONE)));
        return bidLinesDiscountsMap.entrySet().stream()
                .anyMatch(entry -> !deepEquals(entry.getValue(), replacedBidLinesDiscountsMap.get(entry.getKey())));
    }
}
