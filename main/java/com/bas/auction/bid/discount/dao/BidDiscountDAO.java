package com.bas.auction.bid.discount.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.discount.dto.BidDiscount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BidDiscountDAO {
	List<BidDiscount> findBidLineDiscounts(Long bidId, Integer lineNum);

	List<BidDiscount> findBidOriginalDiscounts(Long bidId);

	List<BidDiscount> findBidDiscounts(Long bidId);

	BigDecimal findBidLineTotalDiscount(Long bidId, Integer lineNum);

	Map<Integer, BigDecimal> findBidLinesTotalDiscount(Long bidId);

	boolean findIsNegLineBidsAllDiscountsConfirmed(Long negId, Integer lineNum);

	List<Integer> findDiscountNotConfirmedLineNums(Long negId);

	void insert(User user, Long negId, Long bidId);

	void update(Long userId, Long bidId, List<BidDiscount> discounts);

	void updateBidLineTotalDiscount(Long bidId, Integer lineNum, BigDecimal bidLineTotalDiscount);

	void confirmBidLineDiscounts(Long userId, Long bidId, Integer lineNum);

	void makeCorrection(List<BidDiscount> discounts);

	void deleteNotParticipatedBidLineDiscounts(Long negId);

	void deleteTender2Stage1NotParticipatedBidLineDiscounts(Long negId);

	Long findDomesticProducerOfGoodsDiscountId(Long negId);

	Long findWorkServiceLocalContentDiscountId(Long negId);

	Long findExperienceDiscountId(Long negId);

	Boolean findBidLineDiscountBooleanValue(Long bidId, Integer lineNum, Long discountId);

	BigDecimal findBidLineDiscountNumberValue(Long bidId, Integer lineNum, Long discountId);

	void deleteBidDiscounts(Long bidId);

	void copyBidDiscounts(User user, Long sourceBidId, Long destinationBidId);

	void copyBidActiveDiscounts(User user, Long sourceBidId, Long destinationBidId);
}
