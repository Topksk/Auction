package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.award.service.NegLineAwardAndBidLinesRankService;
import com.bas.auction.neg.award.service.tiebreaker.BidRanksTiebreaker;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DiscountedBidPriceBasedAwardAndRankService implements NegLineAwardAndBidLinesRankService {
    private final Logger logger = LoggerFactory.getLogger(DiscountedBidPriceBasedAwardAndRankService.class);
    private final NegLineDAO negLineDAO;
    private final BidDiscountService bidDiscountService;
    private final BidDAO bidDAO;
    private final Long userId;
    private final Long negId;
    private final Integer lineNum;
    protected NegLine awardedNegLine;
    protected List<BidLine> rankedBidLines;
    private Map<Long, Long> suppliers;

    public DiscountedBidPriceBasedAwardAndRankService(NegLineDAO negLineDAO, BidDiscountService bidDiscountService,
                                                      BidDAO bidDAO, Long userId, Long negId, Integer lineNum) {
        this.negLineDAO = negLineDAO;
        this.bidDiscountService = bidDiscountService;
        this.bidDAO = bidDAO;
        this.userId = userId;
        this.negId = negId;
        this.lineNum = lineNum;
    }

    @Override
    public void awardNegLineAndRankBidLines() {
        logger.debug("award neg line and rank bid lines: negId={}, lineNum={}", negId, lineNum);
        awardedNegLine = createAwardedNegLine();
        List<Map<String, Long>> bidRanks = negLineDAO.findDiscountedPriceBasedNegLineBidRanks(negId, lineNum);
        suppliers = bidRanks.stream()
                .collect(toMap(bidRank -> bidRank.get("bid_id"), bidRank -> bidRank.get("supplier_id")));
        rankedBidLines = bidRanks.stream().map(this::rankBidLine).collect(toList());
        buildTiebreaker().breakTies(rankedBidLines);
        rankedBidLines.forEach(this::determineBidLineStatus);
    }

    private BidLine rankBidLine(Map<String, Long> bidRank) {
        Long bidId = bidRank.get("bid_id");
        Long rank = bidRank.get("bid_rank");
        BidLine bidLine = new BidLine();
        bidLine.setBidId(bidId);
        bidLine.setLineNum(lineNum);
        bidLine.setRank(rank.intValue());
        bidLine.setLastUpdatedBy(userId);
        return bidLine;
    }

    private void determineBidLineStatus(BidLine bidLine) {
        boolean awarded = bidLine.getRank() == 1; // bid with rank 1 is winner
        String bidLineStatus = awarded ? "AWARDED" : "FAILED";
        bidLine.setBidLineStatus(bidLineStatus);
        if (awarded) {
            Long supplierId = suppliers.get(bidLine.getBidId());
            awardedNegLine.setWinnerSupplierId(supplierId);
        }
    }

    private NegLine createAwardedNegLine() {
        NegLine negLine = new NegLine();
        negLine.setNegId(negId);
        negLine.setLineNum(lineNum);
        negLine.setLastUpdatedBy(userId);
        return negLine;
    }

    protected BidRanksTiebreaker buildTiebreaker() {
        return new BidRanksTiebreaker(negLineDAO, bidDiscountService, bidDAO, negId, lineNum);
    }

    @Override
    public NegLine getAwardedNegLine() {
        return awardedNegLine;
    }

    @Override
    public List<BidLine> getRankedBidLines() {
        return rankedBidLines;
    }
}
