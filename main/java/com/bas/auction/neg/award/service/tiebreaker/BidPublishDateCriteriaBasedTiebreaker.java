package com.bas.auction.neg.award.service.tiebreaker;

import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dto.BidLine;

public class BidPublishDateCriteriaBasedTiebreaker implements CriteriaBasedTiebreaker {
	private final BidDAO bidDAO;
	private Map<Long, Integer> publishDateRanks;

	public BidPublishDateCriteriaBasedTiebreaker(BidDAO bidDAO) {
		this.bidDAO = bidDAO;
	}

	@Override
	public void breakTies(List<BidLine> rankTiedBidLines) {
		List<Long> bidIds = rankTiedBidLines.stream().map(BidLine::getBidId).collect(toList());
		Map<Long, Date> bidDates = bidDAO.findPublishDates(bidIds);
		publishDateRanks = rankAscending(bidDates);
		rankTiedBidLines.forEach(this::incrementRanks);
	}

	private void incrementRanks(BidLine bidLine) {
		Integer bidRank = bidLine.getRank();
		Integer sentDateRank = publishDateRanks.get(bidLine.getBidId());
		bidLine.setRank(bidRank + sentDateRank);
	}
}
