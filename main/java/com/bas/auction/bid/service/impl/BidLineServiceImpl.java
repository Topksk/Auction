package com.bas.auction.bid.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dto.Negotiation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BidLineServiceImpl implements BidLineService {
    private final static Logger logger = LoggerFactory.getLogger(BidLineServiceImpl.class);
    private final BidDiscountService bidDiscountService;
    private final BidLineDAO bidLineDAO;

    @Autowired
    public BidLineServiceImpl(BidLineDAO bidLineDAO, BidDiscountService bidDiscountService) {
        this.bidLineDAO = bidLineDAO;
        this.bidDiscountService = bidDiscountService;
    }

    @Override
    @SpringTransactional
    public void createBidLinesFromNeg(User user, Long negId, Long bidId) {
        logger.debug("create bid lines from neg: negId={}, bidId={}", negId, bidId);
        bidLineDAO.insert(user, negId, bidId);
    }

    @Override
    public void copyBidLinesForReplace(User user, Long sourceBidId, Long destinationBidId) {
        logger.debug("copy bid lines for replace: sourceBidId={}, destinationBidId={}", sourceBidId, destinationBidId);
        bidLineDAO.copyBidLinesForReplace(user, sourceBidId, destinationBidId);
    }

    @Override
    @SpringTransactional
    public void copyBidLinesForTender2Stage2(User user, Long sourceBidId, Long destinationBidId) {
        logger.debug("copy bid lines for tender2 stage2: sourceBidId={}, destinationBidId={}", sourceBidId, destinationBidId);
        bidLineDAO.copyBidLinesForTender2Stage2(user, sourceBidId, destinationBidId);
    }

    @Override
    @SpringTransactional
    public void update(User user, Long bidId, List<BidLine> bidLines) {
        logger.debug("update bid lines: bidId={}", bidId);
        bidLines.forEach(line -> setBidLineValues(user, bidId, line));
        bidLineDAO.update(bidLines);
    }

    private void setBidLineValues(User user, Long bidId, BidLine line) {
        line.setLastUpdatedBy(user.getUserId());
        line.setBidId(bidId);
    }

    @Override
    @SpringTransactional
    public void deleteNotParticipatedBidLines(Negotiation neg) {
        logger.debug("delete not participated bid lines: negId={}", neg.getNegId());
        bidDiscountService.deleteNotParticipatedBidLineDiscounts(neg.getNegId(), neg.isTender2Stage1());
        if (neg.isTender2Stage1()) {
            logger.debug("delete tender2 stage1 not participated bid lines");
            bidLineDAO.deleteTender2Stage1NotParticipatedBidLines(neg.getNegId());
        } else {
            bidLineDAO.deleteNotParticipatedBidLines(neg.getNegId());
        }
    }

    @Override
    @SpringTransactional
    public void resetAwardStatusesAndRanks(Long userId, Long negId) {
        logger.debug("reset award statuses and ranks: negId={}", negId);
        bidLineDAO.resetAwardStatusesAndRanks(userId, negId);
    }

    @Override
    @SpringTransactional
    public void deleteBidLines(Long bidId) {
        logger.debug("delete bid lines: bidId={}", bidId);
        bidLineDAO.deleteBidLines(bidId);
    }
}
