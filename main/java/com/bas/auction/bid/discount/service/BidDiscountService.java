package com.bas.auction.bid.discount.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.bid.dto.BidLine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BidDiscountService {
	List<BidDiscount> findBidLineDiscounts(Long bidId, Integer lineNum);

	List<BidDiscount> findBidOriginalDiscounts(Long bidId);

	List<BidDiscount> findBidDiscounts(Long bidId);

	Map<Integer, BigDecimal> findBidLinesTotalDiscounts(Long bidId);

	void createBidDiscounts(User user, Long negId, Long bidId);

	void copyBidDiscounts(User user, Long sourceBidId, Long destinationBidId);

	void copyBidActiveDiscounts(User user, Long sourceBidId, Long destinationBidId);

	void update(Long userId, Long bidId, List<BidDiscount> discounts);

	void correctBidLineDiscountsAndConfirm(Long userId, Long bidId, Integer lineNum, List<BidDiscount> discounts);

	void deleteNotParticipatedBidLineDiscounts(Long negId, boolean isTender2Stage1);

	Map<Long, Boolean> findIsDomesticProducerOfGoods(Long negId, List<BidLine> bidLines);

	Map<Long, Boolean> findIsDomesticWorkService(Long negId, List<BidLine> bidLines);

	Map<Long, BigDecimal> findExperiences(Long negId, List<BidLine> bidLines);

	void deleteBidDiscounts(Long bidId);
}
