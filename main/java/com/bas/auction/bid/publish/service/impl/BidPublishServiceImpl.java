package com.bas.auction.bid.publish.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.bid.publish.service.BidPublishService;
import com.bas.auction.bid.publish.service.BidPublishValidationService;
import com.bas.auction.bid.publish.service.BidReportValidationService;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.bid.service.BidNotificationService;
import com.bas.auction.bid.service.BidReportService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.neg.auction.service.AuctionExtendService;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toList;

@Service
public class BidPublishServiceImpl implements BidPublishService {
    private final Logger logger = LoggerFactory.getLogger(BidPublishServiceImpl.class);
    private final BidDAO bidDAO;
    private final BidLineDAO bidLineDAO;
    private final NegotiationDAO negDAO;
    private final NegLineDAO negLineDAO;
    private final AuctionExtendService auctionExtendService;
    private final BidReportService bidReportService;
    private final ExchangeRateService exchangeRateService;
    private final BidNotificationService bidNotificationService;
    private final BidFileService bidFileService;
    private final BidPublishValidationService bidPublishValidationService;
    private final BidDiscountService bidDiscountService;
    private final BidReportValidationService bidReportValidationService;

    @Autowired
    public BidPublishServiceImpl(BidDAO bidDAO, BidLineDAO bidLineDAO, NegotiationDAO negDAO, NegLineDAO negLineDAO,
                                 AuctionExtendService auctionExtendService, BidReportService bidReportService,
                                 ExchangeRateService exchangeRateService, BidNotificationService bidNotificationService,
                                 BidFileService bidFileService, BidPublishValidationService bidPublishValidationService,
                                 BidDiscountService bidDiscountService, BidReportValidationService bidReportValidationService, SimpMessageSendingOperations messagingTemplate) {
        this.bidDAO = bidDAO;
        this.bidLineDAO = bidLineDAO;
        this.negDAO = negDAO;
        this.negLineDAO = negLineDAO;
        this.auctionExtendService = auctionExtendService;
        this.bidReportService = bidReportService;
        this.exchangeRateService = exchangeRateService;
        this.bidNotificationService = bidNotificationService;
        this.bidFileService = bidFileService;
        this.bidPublishValidationService = bidPublishValidationService;
        this.bidDiscountService = bidDiscountService;
        this.bidReportValidationService = bidReportValidationService;
    }

    @Override
    @SpringTransactional
    public Bid send(User user, Long bidId) throws MessagingException {
        logger.info("send bid: {}", bidId);
        Long negId = bidDAO.findBidNegId(bidId);
        NegType negType = negDAO.findNegType(negId);
        bidPublishValidationService.validateBidSend(bidId, negId, negType);

        updateBidForSend(user, bidId);
        updateBidLines(user, bidId, negType);

        Bid bid = bidDAO.findById(user, bidId);
        if (bid.isReplacesOtherBid())
            updateReplacedBid(user, bid);
        if (negType == NegType.AUCTION)
            auctionExtendService.extendAuction(negId);
        negLineDAO.updateBidCount(negId);
        bidDAO.indexSync(bid);
        if (negType == NegType.AUCTION) {
            bidNotificationService.sendAuctionPriceChangeNotif(user, bid);
        }
        bidNotificationService.sendBidSentNotification(user, bid);
        return bid;
    }

    protected void updateBidForSend(User user, Long bidId) {
        String bidCurrency = bidDAO.findBidCurrency(bidId);
        BigDecimal exchangeRate = exchangeRateService.findCurrentExchangeRate(bidCurrency);
        bidDAO.updateBidForPublish(user.getUserId(), bidId, exchangeRate);
        bidFileService.makeBidFilesReadOnly(user.getUserId(), bidId);
    }

    protected void updateBidLines(User user, Long bidId, NegType negType) {
        if (negType == NegType.TENDER || negType == NegType.TENDER2) {
            updateBidLinesDiscounts(bidId);
        }
        bidLineDAO.updateParticipatingBidLinesStatuses(user.getUserId(), bidId, "ACTIVE");
    }

    protected void updateBidLinesDiscounts(Long bidId) {
        Map<Integer, BigDecimal> bidLinesTotalDiscounts = bidDiscountService.findBidLinesTotalDiscounts(bidId);
        List<BidLine> bidLines = bidLinesTotalDiscounts.entrySet().stream()
                .map(e -> mapEntryToBidLine(bidId, e))
                .collect(toList());
        bidLineDAO.updateBidLineTotalDiscount(bidLines);
    }

    protected BidLine mapEntryToBidLine(Long bidId, Entry<Integer, BigDecimal> entry) {
        BidLine bidLine = new BidLine();
        BigDecimal discount = entry.getValue();
        boolean discountConfirmed = discount == null || BigDecimal.ZERO.equals(discount);
        bidLine.setBidId(bidId);
        bidLine.setLineNum(entry.getKey());
        bidLine.setDiscount(discount);
        bidLine.setDiscountConfirmed(discountConfirmed);
        return bidLine;
    }

    protected void updateReplacedBid(User user, Bid bid) {
        logger.info("active bid replaced: currBidId = {}, replacedBidId = {}", bid.getBidId(),
                bid.getReplacedBidId());
        bidDAO.updateStatus(user.getUserId(), bid.getReplacedBidId(), "REPLACED");
        bidLineDAO.updateStatuses(user.getUserId(), bid.getReplacedBidId(), "REPLACED");
        bidDAO.findAndIndexSync(user, bid.getReplacedBidId());
    }

    @Override
    @SpringTransactional
    public Bid generateBidReport(User user, Long bidId) throws Exception {
        generateBidReportWithoutIndexing(user, bidId);
        return bidDAO.findAndIndexSync(user, bidId);
    }

    @Override
    @SpringTransactional
    public Bid generateBidReportAndParticipationAppl(User user, Long bidId) throws Exception {
        generateBidReportWithoutIndexing(user, bidId);
        return generateBidParticipationAppl(user, bidId);
    }

    @Override
    @SpringTransactional
    public Bid generateBidParticipationAppl(User user, Long bidId) throws Exception {
        logger.debug("generating bid participation appl: bidId={}", bidId);
        Long negId = bidDAO.findBidNegId(bidId);
        boolean isTender2Stage1 = negDAO.findIsTender2Stage1(negId);
        if (isTender2Stage1) {
            bidReportValidationService.validateTender2Stage1BidParticipationAppl(user, bidId);
            bidFileService.makeBidFilesReadOnly(user.getUserId(), bidId);
        }
        bidReportService.generateBidParticipationAppl(user, bidId);
        return bidDAO.findAndIndexSync(user, bidId);
    }

    protected void generateBidReportWithoutIndexing(User user, Long bidId) throws Exception {
        logger.debug("generating bid report: bidId={}", bidId);
        bidReportValidationService.validateBidReport(user, bidId);
        updateBidCurrencyExchangeRate(user, bidId);
        bidFileService.makeBidFilesReadOnly(user.getUserId(), bidId);
        bidReportService.generateBidReport(user, bidId);
    }

    protected void updateBidCurrencyExchangeRate(User user, Long bidId) {
        logger.debug("update bid currency exchange rate: bidId={}", bidId);
        String currency = bidDAO.findBidCurrency(bidId);
        BigDecimal exchangeRate = exchangeRateService.findCurrentExchangeRate(currency);
        bidDAO.updateBidExchangeRate(user, bidId, exchangeRate);
    }
}
