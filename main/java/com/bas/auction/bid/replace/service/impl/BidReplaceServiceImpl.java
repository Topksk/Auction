package com.bas.auction.bid.replace.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.replace.service.BidAlreadyReplacedException;
import com.bas.auction.bid.replace.service.BidReplaceService;
import com.bas.auction.bid.replace.service.OnlyActiveBidCanBeReplacedException;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.bid.service.BidSendFinishedException;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BidReplaceServiceImpl implements BidReplaceService {
    private final Logger logger = LoggerFactory.getLogger(BidReplaceServiceImpl.class);
    private final NegotiationDAO negotiationDAO;
    private final BidDAO bidDAO;
    private final BidLineService bidLineService;
    private final BidDiscountService bidDiscountService;
    private final BidFileService bidFileService;

    @Autowired
    public BidReplaceServiceImpl(NegotiationDAO negotiationDAO, BidLineService bidLineService, BidDAO bidDAO, BidDiscountService bidDiscountService, BidFileService bidFileService) {
        this.negotiationDAO = negotiationDAO;
        this.bidLineService = bidLineService;
        this.bidDAO = bidDAO;
        this.bidDiscountService = bidDiscountService;
        this.bidFileService = bidFileService;
    }

    @Override
    @SpringTransactional
    public Bid replace(User user, Long bidId) throws IOException {
        logger.debug("replace bid: bidId={}", bidId);
        Long replacingBidId = bidDAO.findReplacingBidId(bidId);
        if (replacingBidId != null) {
            logger.debug("bid already replaced: newBidId={}", replacingBidId);
            return bidDAO.findById(user, replacingBidId);
        }
        Long negId = bidDAO.findBidNegId(bidId);
        validateReplace(bidId, negId);
        NegType negType = negotiationDAO.findNegType(negId);
        Long newBidId = bidDAO.copyBidForReplace(user, bidId);
        bidLineService.copyBidLinesForReplace(user, bidId, newBidId);
        if (negType == NegType.TENDER || negType == NegType.TENDER2)
            bidDiscountService.copyBidDiscounts(user, bidId, newBidId);
        bidFileService.copyBidFiles(user, bidId, newBidId);
        Bid bid = bidDAO.findAndIndexSync(user, newBidId);
        negotiationDAO.reindexBidIds(negId);
        return bid;
    }

    private void validateReplace(Long bidId, Long negId) {
        boolean isBidReplaced = bidDAO.findIsBidReplaced(bidId);
        if (isBidReplaced) {
            throw new BidAlreadyReplacedException();
        }
        boolean isPublishedNeg = negotiationDAO.findIsPublishedNeg(negId);
        if (!isPublishedNeg) {
            throw new BidSendFinishedException();
        }
        String bidStatus = bidDAO.findBidStatus(bidId);
        if (!"ACTIVE".equals(bidStatus))
            throw new OnlyActiveBidCanBeReplacedException();
    }

}
