package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.award.service.NegLineAwardAndBidLinesRankService;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class BidPriceBasedAwardAndRankService implements NegLineAwardAndBidLinesRankService {
    private final Logger logger = LoggerFactory.getLogger(BidPriceBasedAwardAndRankService.class);
    private final NegLineDAO negLineDAO;
    private final Long userId;
    private final Long negId;
    private final Integer lineNum;
    protected NegLine awardedNegLine;
    protected List<BidLine> rankedBidLines;

    public BidPriceBasedAwardAndRankService(NegLineDAO negLineDAO, Long userId, Long negId, Integer lineNum) {
        this.negLineDAO = negLineDAO;
        this.userId = userId;
        this.negId = negId;
        this.lineNum = lineNum;
    }

    @Override
    public void awardNegLineAndRankBidLines() {
        logger.debug("award neg line and rank bid lines: negId={}, lineNum={}", negId, lineNum);
        awardedNegLine = createAwardedNegLine();
        List<Map<String, Long>> bidRanks = negLineDAO.findPriceBasedNegLineBidRanks(negId, lineNum);
        rankedBidLines = bidRanks.stream()
                .map(this::rankBidLine)
                .collect(toList());
    }

    private BidLine rankBidLine(Map<String, Long> bidRank) {
        Long bidId = bidRank.get("bid_id");
        Long rank = bidRank.get("bid_rank");
        boolean awarded = rank == 1; // bid with rank 1 is winner
        String bidLineStatus = awarded ? "AWARDED" : "FAILED";
        if (awarded) {
            Long supplierId = bidRank.get("supplier_id");
            awardedNegLine.setWinnerSupplierId(supplierId);
        }
        return createRankedBidLine(bidId, rank, bidLineStatus);
    }

    private BidLine createRankedBidLine(Long bidId, Long rank, String bidLineStatus) {
        BidLine bidLine = new BidLine();
        bidLine.setBidId(bidId);
        bidLine.setLineNum(lineNum);
        bidLine.setRank(rank.intValue());
        bidLine.setBidLineStatus(bidLineStatus);
        bidLine.setLastUpdatedBy(userId);
        return bidLine;
    }

    private NegLine createAwardedNegLine() {
        NegLine negLine = new NegLine();
        negLine.setNegId(negId);
        negLine.setLineNum(lineNum);
        negLine.setLastUpdatedBy(userId);
        return negLine;
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
