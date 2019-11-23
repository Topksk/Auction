package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class NegLineFailService {
    private final Logger logger = LoggerFactory.getLogger(NegLineFailService.class);
    private final NegLineDAO negLineDAO;
    private final Long userId;
    private final Long negId;
    private final Integer lineNum;
    protected Optional<NegLine> failedNegLine;
    protected Optional<BidLine> failedBidLine;

    public NegLineFailService(NegLineDAO negLineDAO, Long userId, Long negId, Integer lineNum) {
        this.negLineDAO = negLineDAO;
        this.userId = userId;
        this.negId = negId;
        this.lineNum = lineNum;
    }

    public void failNegLine() {
        logger.debug("fail neg: negId={}, lineNum={}", negId, lineNum);
        Map<String, Long> bidCounts = negLineDAO.findNegLineTotalAndPermittedBidCounts(negId, lineNum);
        long totalBidCount = bidCounts.get("total_bid_count");
        long permittedBidCount = bidCounts.get("permitted_bid_count");
        logger.debug("totalBidCount={}, permittedBidCount={}", totalBidCount, permittedBidCount);
        if (permittedBidCount >= 2) {
            failedNegLine = Optional.empty();
            failedBidLine = Optional.empty();
            return;
        }
        failedNegLine = createFailedNegLine();
        if (totalBidCount == 0) {
            failNegLinesWithNoBid();
        } else if (totalBidCount == 1) {
            failNegLinesWithLessThanTwoBid();
        } else {
            failNegLinesWithLessThanTwoPermittedBids();
        }
    }

    private void failNegLinesWithNoBid() {
        logger.debug("failed neg line: negId = {}, lineNum = {}, reason = NO_BIDS", negId, lineNum);
        failedNegLine.get().setFailReason("NO_BIDS");
        failedBidLine = Optional.empty();
    }

    private void failNegLinesWithLessThanTwoBid() {
        logger.debug("failed neg line: negId = {}, lineNum = {}, reason = LESS_THAN_TWO_BIDS", negId, lineNum);
        failedNegLine.get().setFailReason("LESS_THAN_TWO_BIDS");
        failedBidLine = createFailedBidLine();
    }

    private void failNegLinesWithLessThanTwoPermittedBids() {
        logger.debug("failed neg line: negId = {}, lineNum = {}, reason = LESS_THAN_TWO_PERMITTED_BIDS", negId, lineNum);
        failedNegLine.get().setFailReason("LESS_THAN_TWO_PERMITTED_BIDS");
        failedBidLine = createFailedBidLine();
    }

    private Optional<NegLine> createFailedNegLine() {
        NegLine negLine = new NegLine();
        negLine.setNegId(negId);
        negLine.setLineNum(lineNum);
        negLine.setLastUpdatedBy(userId);
        return Optional.of(negLine);
    }

    private Optional<BidLine> createFailedBidLine() {
        BidLine bidLine = new BidLine();
        bidLine.setLineNum(lineNum);
        bidLine.setBidLineStatus("FAILED");
        bidLine.setLastUpdatedBy(userId);
        return Optional.of(bidLine);
    }

}
