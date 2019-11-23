package com.bas.auction.neg.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.NegLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface NegLineDAO {
	List<NegLine> findNegLines(Long negId);

	List<NegLine> findBidNegLines(Long negId, Long bidId);

	Map<Integer, BigDecimal> findNegLineUnitPrices(Long negId);

	Boolean findIsDiscountConfirmed(Long bidId, Integer lineNum);

	void create(User user, Long negId, List<Long> planIds);

	void copyNotFailedNegLines(User user, Long sourceNegId, Long destinationNegId);

	void delete(Long negId, List<Long> planIds);

	void deleteNegLines(Long negId);

	List<Integer> findNegLineNums(Long negId);

	Map<String, Long> findNegLineTotalAndPermittedBidCounts(Long negId, Integer lineNum);

	List<Map<String, Long>> findPriceBasedNegLineBidRanks(Long negId, Integer lineNum);

	void finalizeNegLines(List<NegLine> negLines);

	List<Map<String, Long>> findDiscountedPriceBasedNegLineBidRanks(Long negId, Integer lineNum);

	String findNegLinePurchaseType(Long negId, Integer lineNum);

	void resetAwards(Long userId, Long negId);

	void updateBidCount(Long negId);

	List<Integer> findNotFailedNegLineNums(Long negId);

	List<Map<String, Object>> findForIntegra(long negId);

	BigDecimal findNegLinesMinUnitPrice(Long negId);

	void confirmNegLineDiscounts(Long userId, Long negId, Integer lineNum);
}
