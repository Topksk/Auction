package com.bas.auction.bid.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.dto.Negotiation;

import java.util.List;

public interface BidLineService {
	void createBidLinesFromNeg(User user, Long negId, Long bidId);

	void copyBidLinesForReplace(User user, Long sourceBidId, Long destinationBidId);

	void copyBidLinesForTender2Stage2(User user, Long sourceBidId, Long destinationBidId);

	void update(User user, Long bidId, List<BidLine> bidLines);

	void deleteNotParticipatedBidLines(Negotiation neg);

	void resetAwardStatusesAndRanks(Long userId, Long negId);

	void deleteBidLines(Long bidId);
}
