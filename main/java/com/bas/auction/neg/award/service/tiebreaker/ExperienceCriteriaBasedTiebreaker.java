package com.bas.auction.neg.award.service.tiebreaker;

import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExperienceCriteriaBasedTiebreaker implements CriteriaBasedTiebreaker {
    private final Logger logger = LoggerFactory.getLogger(ExperienceCriteriaBasedTiebreaker.class);
    private final BidDiscountService bidDiscountService;
    private final Long negId;
    private Map<Long, Integer> experienceRanks;

    public ExperienceCriteriaBasedTiebreaker(BidDiscountService bidDiscountService, Long negId) {
        this.bidDiscountService = bidDiscountService;
        this.negId = negId;
    }

    @Override
    public void breakTies(List<BidLine> rankTiedBidLines) {
        logger.debug("break ties using experience criteria");
        Map<Long, BigDecimal> experiences = bidDiscountService.findExperiences(negId, rankTiedBidLines);
        logger.debug("experiences {}", experiences);
        experienceRanks = rankDescending(experiences);
        logger.debug("experience ranks {}", experienceRanks);
        rankTiedBidLines.forEach(this::incrementRanks);
    }

    private void incrementRanks(BidLine bidLine) {
        Integer bidRank = bidLine.getRank();
        Integer experienceRank = experienceRanks.get(bidLine.getBidId());
        bidLine.setRank(bidRank + experienceRank);
    }
}
