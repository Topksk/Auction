package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegLineAwardAndBidLinesRankService;
import com.bas.auction.neg.dao.NegLineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class TenderAwardAndRankService {
    private final Logger logger = LoggerFactory.getLogger(TenderAwardAndRankService.class);
    private final NegLineDAO negLineDAO;
    private final BidLineDAO bidLineDAO;
    private final BidDiscountService bidDiscountService;
    private final BidDAO bidDAO;

    @Autowired
    public TenderAwardAndRankService(NegLineDAO negLineDAO, BidLineDAO bidLineDAO, BidDAO bidDAO,
                                     BidDiscountService bidDiscountService) {
        this.negLineDAO = negLineDAO;
        this.bidLineDAO = bidLineDAO;
        this.bidDAO = bidDAO;
        this.bidDiscountService = bidDiscountService;
    }

    @SpringTransactional
    public void finalize(Long userId, Long negId) {
        logger.debug("finalize tender: negId={}", negId);
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
        return negLineNum -> new DiscountedBidPriceBasedAwardAndRankService(negLineDAO, bidDiscountService, bidDAO,
                userId, negId, negLineNum);
    }

    protected GenericAwardAndRankService buildAwardingService(Long negId) {
        return new GenericAwardAndRankService(negLineDAO, bidLineDAO, negId);
    }
}
