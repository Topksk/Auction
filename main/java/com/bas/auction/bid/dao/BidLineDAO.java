package com.bas.auction.bid.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.BidLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BidLineDAO {
	List<BidLine> findBidLines(Long bidId);

	Map<Integer, BigDecimal> findBidLinePrices(Long bidId);

	void insert(User user, Long negId, Long bidId);

	void copyBidLinesForReplace(User user, Long sourceBidId, Long destinationBidId);

	void copyBidLinesForTender2Stage2(User user, Long sourceBidId, Long destinationBidId);

	void updateStatuses(Long userId, Long bidId, String status);

	void updateParticipatingBidLinesStatuses(Long userId, Long bidId, String status);

	void updateBidLineTotalDiscount(List<BidLine> bidLines);

	void update(List<BidLine> bidLines);

	void deleteNotParticipatedBidLines(Long negId);

	void deleteTender2Stage1NotParticipatedBidLines(Long negId);

	void failBidLines(Long negId, List<BidLine> failedBidLines);

	void updateRejectedBidLineStatuses(Long negId);

	void rankBidLines(List<BidLine> rankedBidLines);

	void resetAwardStatusesAndRanks(Long userId, Long negId);

	void deleteBidLines(Long bidId);
}
