package com.bas.auction.neg.award.service.impl;

import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.award.service.NegLineAwardAndBidLinesRankService;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GenericAwardAndRankService {
	private final NegLineDAO negLineDAO;
	private final BidLineDAO bidLineDAO;
	private List<Integer> lineNums;
	private List<NegLine> negLines = new ArrayList<>();
	private List<BidLine> rankedBidLines = new ArrayList<>();
	private final Long negId;

	public GenericAwardAndRankService(NegLineDAO negLineDAO, BidLineDAO bidLineDAO, Long negId) {
		this.negLineDAO = negLineDAO;
		this.bidLineDAO = bidLineDAO;
		this.negId = negId;
	}

	public void award(Function<Integer, NegLineAwardAndBidLinesRankService> negLineToAwardServiceMapper) {
		lineNums = negLineDAO.findNotFailedNegLineNums(negId);

		awardNegLines(negLineToAwardServiceMapper);

		negLineDAO.finalizeNegLines(negLines);
		bidLineDAO.rankBidLines(rankedBidLines);
	}

	public void awardNegLines(Function<Integer, NegLineAwardAndBidLinesRankService> negLineToAwardServiceMapper) {
		lineNums.stream().map(negLineToAwardServiceMapper).forEach(this::awardNegLine);
	}

	public void awardNegLine(NegLineAwardAndBidLinesRankService service) {
		service.awardNegLineAndRankBidLines();
		negLines.add(service.getAwardedNegLine());
		rankedBidLines.addAll(service.getRankedBidLines());
	}
}
