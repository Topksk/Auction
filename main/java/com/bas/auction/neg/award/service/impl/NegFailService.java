package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NegFailService {
    private final Logger logger = LoggerFactory.getLogger(NegFailService.class);
    private final NegLineDAO negLineDAO;
    private final BidLineDAO bidLineDAO;
    private List<NegLine> negLines = new ArrayList<>();
    private List<BidLine> failedBidLines = new ArrayList<>();
    private final Long userId;
    private final Long negId;

    public NegFailService(NegLineDAO negLineDAO, BidLineDAO bidLineDAO, Long userId, Long negId) {
        this.negLineDAO = negLineDAO;
        this.bidLineDAO = bidLineDAO;
        this.userId = userId;
        this.negId = negId;
    }

    public void failNeg() {
        logger.debug("fail neg: {}", negId);
        List<Integer> lineNums = negLineDAO.findNegLineNums(negId);
        lineNums.stream().map(this::mapToNegLineFailService).forEach(this::failNegLine);

        negLineDAO.finalizeNegLines(negLines);
        bidLineDAO.failBidLines(negId, failedBidLines);
        bidLineDAO.updateRejectedBidLineStatuses(negId);
    }

    protected NegLineFailService mapToNegLineFailService(Integer lineNum) {
        return new NegLineFailService(negLineDAO, userId, negId, lineNum);
    }

    private void failNegLine(NegLineFailService service) {
        service.failNegLine();
        service.failedNegLine.ifPresent(negLines::add);
        service.failedBidLine.ifPresent(failedBidLines::add);
    }
}
