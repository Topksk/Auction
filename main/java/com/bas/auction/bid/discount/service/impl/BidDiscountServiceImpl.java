package com.bas.auction.bid.discount.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.dao.BidDiscountDAO;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.bid.discount.service.BidDiscountCorrectionReasonRequiredException;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.discount.service.BidDiscountValidationService;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegLineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Service
public class BidDiscountServiceImpl implements BidDiscountService {
    private final Logger logger = LoggerFactory.getLogger(BidDiscountServiceImpl.class);
    private final BidDAO bidDAO;
    private final BidDiscountDAO bidDiscountDAO;
    private final NegLineDAO negLineDAO;
    private final BidDiscountValidationService bidDiscountValidationService;

    @Autowired
    public BidDiscountServiceImpl(BidDAO bidDAO, BidDiscountDAO bidDiscountDAO, NegLineDAO negLineDAO, BidFileService bidFileService, BidDiscountValidationService bidDiscountValidationService) {
        this.bidDAO = bidDAO;
        this.bidDiscountDAO = bidDiscountDAO;
        this.negLineDAO = negLineDAO;
        this.bidDiscountValidationService = bidDiscountValidationService;
    }

    @Override
    public List<BidDiscount> findBidLineDiscounts(Long bidId, Integer lineNum) {
        return bidDiscountDAO.findBidLineDiscounts(bidId, lineNum);
    }

    @Override
    public List<BidDiscount> findBidOriginalDiscounts(Long bidId) {
        return bidDiscountDAO.findBidOriginalDiscounts(bidId);
    }

    @Override
    public List<BidDiscount> findBidDiscounts(Long bidId) {
        return bidDiscountDAO.findBidDiscounts(bidId);
    }

    @Override
    public Map<Long, Boolean> findIsDomesticProducerOfGoods(Long negId, List<BidLine> bidLines) {
        Long discountId = bidDiscountDAO.findDomesticProducerOfGoodsDiscountId(negId);
        logger.debug("domestic good producer: negId={}, discountId={}", negId, discountId);
        return bidLines.stream()
                .map(bidLine -> mapToIsDomesticProducerOfGoods(bidLine, discountId))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Long, Boolean> mapToIsDomesticProducerOfGoods(BidLine bidLine, Long discountId) {
        Boolean isDomestic = bidDiscountDAO.findBidLineDiscountBooleanValue(bidLine.getBidId(), bidLine.getLineNum(),
                discountId);
        isDomestic = Optional.ofNullable(isDomestic).orElse(Boolean.FALSE);
        return new SimpleEntry<>(bidLine.getBidId(), isDomestic);
    }

    @Override
    public Map<Long, Boolean> findIsDomesticWorkService(Long negId, List<BidLine> bidLines) {
        Long discountId = bidDiscountDAO.findWorkServiceLocalContentDiscountId(negId);
        logger.debug("domestic work service: negId={}, discountId={}", negId, discountId);
        return bidLines.stream()
                .map(bidLine -> mapToIsDomesticWorkService(bidLine, discountId))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Long, Boolean> mapToIsDomesticWorkService(BidLine bidLine, Long discountId) {
        BigDecimal domesticPercent = bidDiscountDAO.findBidLineDiscountNumberValue(bidLine.getBidId(),
                bidLine.getLineNum(), discountId);
        boolean isDomestic = false;
        if (domesticPercent != null) {
            BigDecimal domesticThreshold = new BigDecimal("95");
            // local content should not be less than 95%
            isDomestic = domesticPercent.compareTo(domesticThreshold) >= 0;
        }
        return new SimpleEntry<>(bidLine.getBidId(), isDomestic);
    }

    @Override
    public Map<Long, BigDecimal> findExperiences(Long negId, List<BidLine> bidLines) {
        Long discountId = bidDiscountDAO.findExperienceDiscountId(negId);
        logger.debug("experience discount: negId={}, discountId={}", negId, discountId);
        return bidLines.stream()
                .map(bidLine -> mapToExperience(bidLine, discountId))
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Long, BigDecimal> mapToExperience(BidLine bidLine, Long discountId) {
        BigDecimal experience = bidDiscountDAO.findBidLineDiscountNumberValue(bidLine.getBidId(), bidLine.getLineNum(),
                discountId);
        experience = Optional.ofNullable(experience).orElse(BigDecimal.ZERO);
        return new SimpleEntry<>(bidLine.getBidId(), experience);
    }

    @Override
    public Map<Integer, BigDecimal> findBidLinesTotalDiscounts(Long bidId) {
        return bidDiscountDAO.findBidLinesTotalDiscount(bidId);
    }

    @Override
    @SpringTransactional
    public void createBidDiscounts(User user, Long negId, Long bidId) {
        logger.debug("create bid discounts: bidId={}", bidId);
        bidDiscountDAO.insert(user, negId, bidId);
    }

    @Override
    @SpringTransactional
    public void copyBidDiscounts(User user, Long sourceBidId, Long destinationBidId) {
        logger.debug("copy bid discounts: sourceBidId={}, destinationBidId={}", sourceBidId, destinationBidId);
        bidDiscountDAO.copyBidDiscounts(user, sourceBidId, destinationBidId);
    }

    @Override
    @SpringTransactional
    public void copyBidActiveDiscounts(User user, Long sourceBidId, Long destinationBidId) {
        logger.debug("copy bid discounts: sourceBidId={}, destinationBidId={}", sourceBidId, destinationBidId);
        bidDiscountDAO.copyBidActiveDiscounts(user, sourceBidId, destinationBidId);
    }

    @Override
    @SpringTransactional
    public void update(Long userId, Long bidId, List<BidDiscount> discounts) {
        logger.debug("update bid discounts: {}", bidId);
        bidDiscountValidationService.validateUpdate(bidId);
        discounts.forEach(discount -> setDiscountValues(discount, userId, bidId, discount.getBidLineNum()));
        bidDiscountDAO.update(userId, bidId, discounts);
    }

    @Override
    @SpringTransactional
    public void correctBidLineDiscountsAndConfirm(Long userId, Long bidId, Integer lineNum, List<BidDiscount> discounts) {
        logger.debug("confirm bid discounts: bidId = {}, lineNum = {}", bidId, lineNum);
        Long negId = bidDAO.findBidNegId(bidId);
        bidDiscountValidationService.validateCorrection(negId);
        makeCorrection(userId, bidId, lineNum, discounts);
        updateBidLineTotalDiscount(bidId, lineNum);
        bidDiscountDAO.confirmBidLineDiscounts(userId, bidId, lineNum);
        boolean isNegLineBidsAllDiscountsConfirmed = bidDiscountDAO.findIsNegLineBidsAllDiscountsConfirmed(negId, lineNum);
        if (isNegLineBidsAllDiscountsConfirmed)
            negLineDAO.confirmNegLineDiscounts(userId, negId, lineNum);
    }

    private void updateBidLineTotalDiscount(Long bidId, Integer lineNum) {
        BigDecimal bidLineTotalDiscount = bidDiscountDAO.findBidLineTotalDiscount(bidId, lineNum);
        bidDiscountDAO.updateBidLineTotalDiscount(bidId, lineNum, bidLineTotalDiscount);
    }

    private void makeCorrection(Long userId, Long bidId, Integer lineNum, List<BidDiscount> discounts) {
        logger.debug("make bid discounts correction: {}", bidId);
        validateCorrection(bidId, lineNum, discounts);
        discounts.forEach(discount -> setDiscountValues(discount, userId, bidId, lineNum));
        bidDiscountDAO.makeCorrection(discounts);
    }

    private void validateCorrection(Long bidId, Integer lineNum, List<BidDiscount> discounts) {
        boolean noCorrectionReason = discounts.stream().anyMatch(this::hasNotCorrectionReason);
        if (noCorrectionReason) {
            logger.error("bid discount correction reason required: bidId = {}, lineNum = {}", bidId, lineNum);
            throw new BidDiscountCorrectionReasonRequiredException();
        }
    }

    private boolean hasNotCorrectionReason(BidDiscount discount) {
        return discount.getCorrectionReason() == null;
    }

    private void setDiscountValues(BidDiscount discount, Long userId, Long bidId, Integer lineNum) {
        discount.setBidId(bidId);
        discount.setBidLineNum(lineNum);
        discount.setLastUpdatedBy(userId);
    }

    @Override
    @SpringTransactional
    public void deleteBidDiscounts(Long bidId) {
        logger.debug("delete bid discounts: bidId={}", bidId);
        bidDiscountDAO.deleteBidDiscounts(bidId);
    }

    @Override
    @SpringTransactional
    public void deleteNotParticipatedBidLineDiscounts(Long negId, boolean isTender2Stage1) {
        logger.debug("delete not participated bid discounts: negId={}, isTender2Stage1={}", negId, isTender2Stage1);
        if (isTender2Stage1)
            bidDiscountDAO.deleteTender2Stage1NotParticipatedBidLineDiscounts(negId);
        else
            bidDiscountDAO.deleteNotParticipatedBidLineDiscounts(negId);
    }

}
