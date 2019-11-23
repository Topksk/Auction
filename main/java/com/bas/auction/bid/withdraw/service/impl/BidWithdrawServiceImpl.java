package com.bas.auction.bid.withdraw.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.service.BidSendFinishedException;
import com.bas.auction.bid.withdraw.service.BidWithdrawService;
import com.bas.auction.bid.withdraw.service.CantWithdrawAuctionBidException;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidWithdrawServiceImpl implements BidWithdrawService {
    private final Logger logger = LoggerFactory.getLogger(BidWithdrawServiceImpl.class);
    private final NegotiationDAO negotiationDAO;
    private final NegLineDAO negLineDAO;
    private final BidDAO bidDAO;

    @Autowired
    public BidWithdrawServiceImpl(NegotiationDAO negotiationDAO, NegLineDAO negLineDAO, BidDAO bidDAO) {
        this.negotiationDAO = negotiationDAO;
        this.negLineDAO = negLineDAO;
        this.bidDAO = bidDAO;
    }

    @Override
    @SpringTransactional
    public Bid withdraw(User user, Long bidId) {
        logger.debug("withdraw bid: bidId={}", bidId);
        Long negId = bidDAO.findBidNegId(bidId);
        validateWithdraw(negId);
        bidDAO.updateStatus(user.getUserId(), bidId, "WITHDRAW");
        negLineDAO.updateBidCount(negId);
        return bidDAO.findAndIndexSync(user, bidId);
    }

    private void validateWithdraw(Long negId) {
        NegType negType = negotiationDAO.findNegType(negId);
        if(negType == NegType.AUCTION)
            throw new CantWithdrawAuctionBidException();
        boolean isPublishedNeg = negotiationDAO.findIsPublishedNeg(negId);
        if(!isPublishedNeg)
            throw new BidSendFinishedException();
    }
}
