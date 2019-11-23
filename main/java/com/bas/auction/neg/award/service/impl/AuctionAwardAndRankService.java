package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegLineAwardAndBidLinesRankService;
import com.bas.auction.neg.dao.NegLineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuctionAwardAndRankService {
    private final Logger logger = LoggerFactory.getLogger(AuctionAwardAndRankService.class);
    private final NegLineDAO negLineDAO;
    private final BidDAO bidDAO;
    private final BidLineDAO bidLineDAO;

    @Autowired
    public AuctionAwardAndRankService(NegLineDAO negLineDAO, BidDAO bidDAO, BidLineDAO bidLineDAO) {
        this.negLineDAO = negLineDAO;
        this.bidDAO = bidDAO;
        this.bidLineDAO = bidLineDAO;
    }

    @SpringTransactional
    public void finalize(Long userId, Long negId) {
        logger.debug("finalize auction: negId={}", negId);
        NegFailService negFailService = buildNegFailService(userId, negId);
        negFailService.failNeg();

        GenericAwardAndRankService service = buildAwardingService(negId);
        service.award(buildNegLineAwardService(userId, negId));

        bidDAO.updateNegActiveBidsAwardStatuses(userId, negId);
    }

    protected NegFailService buildNegFailService(Long userId, Long negId) {
        return new NegFailService(negLineDAO, bidLineDAO, userId, negId);
    }

    protected Function<Integer, NegLineAwardAndBidLinesRankService> buildNegLineAwardService(Long userId, Long negId) {
        return negLineNum -> new BidPriceBasedAwardAndRankService(negLineDAO, userId, negId, negLineNum);
    }

    protected GenericAwardAndRankService buildAwardingService(Long negId) {
        return new GenericAwardAndRankService(negLineDAO, bidLineDAO, negId);
    }
}
