package com.bas.auction.neg.award.service;

import java.util.List;

import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.dto.NegLine;

public interface NegLineAwardAndBidLinesRankService {
	void awardNegLineAndRankBidLines();

	NegLine getAwardedNegLine();

	List<BidLine> getRankedBidLines();
}
