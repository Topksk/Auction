package com.bas.auction.bid.discount.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.discount.dao.BidDiscountDAO;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Repository
class BidDiscountDAOImpl implements BidDiscountDAO, GenericDAO<BidDiscount> {

    private final DaoJdbcUtil daoutil;

    @Autowired
    public BidDiscountDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "bid_discount";
    }

    @Override
    public Class<BidDiscount> getEntityType() {
        return BidDiscount.class;
    }

    @Override
    public List<BidDiscount> findBidLineDiscounts(Long bidId, Integer lineNum) {
        return daoutil.query(this, "get_bid_line_discounts", bidId, lineNum);
    }

    @Override
    public List<BidDiscount> findBidOriginalDiscounts(Long bidId) {
        return daoutil.query(this, "get_bid_discounts_orig", bidId);
    }

    @Override
    public List<BidDiscount> findBidDiscounts(Long bidId) {
        return daoutil.query(this, "get_bid_discounts", bidId);
    }

    @Override
    public Boolean findBidLineDiscountBooleanValue(Long bidId, Integer lineNum, Long discountId) {
        return daoutil.queryScalar(this, "bid_line_discount_boolean_value", bidId, lineNum, discountId);
    }

    @Override
    public BigDecimal findBidLineDiscountNumberValue(Long bidId, Integer lineNum, Long discountId) {
        return daoutil.queryScalar(this, "bid_line_discount_number_value", bidId, lineNum, discountId);
    }

    @Override
    public Long findDomesticProducerOfGoodsDiscountId(Long negId) {
        return daoutil.queryScalar(this, "get_domestic_producer_of_goods_discount_id", negId);
    }

    @Override
    public Long findWorkServiceLocalContentDiscountId(Long negId) {
        return daoutil.queryScalar(this, "get_work_service_local_content_discount_id", negId);
    }

    @Override
    public Long findExperienceDiscountId(Long negId) {
        return daoutil.queryScalar(this, "get_experience_discount_id", negId);
    }

    @Override
    public BigDecimal findBidLineTotalDiscount(Long bidId, Integer lineNum) {
        return daoutil.queryScalar(this, "get_bid_line_total_discount", bidId, lineNum, bidId, lineNum);
    }

    @Override
    public Map<Integer, BigDecimal> findBidLinesTotalDiscount(Long bidId) {
        List<Map<String, Object>> bidLinesDiscounts = daoutil.queryForMapList(this, "get_bid_lines_total_discount", bidId, bidId);
        return bidLinesDiscounts.stream()
                .collect(toMap(m -> (Integer) m.get("bid_line_num"), m -> (BigDecimal) m.get("discount")));
    }

    @Override
    public boolean findIsNegLineBidsAllDiscountsConfirmed(Long negId, Integer lineNum) {
        return daoutil.queryScalar(this, "is_neg_line_bids_all_discounts_confirmed", negId, lineNum);
    }

    @Override
    public List<Integer> findDiscountNotConfirmedLineNums(Long negId) {
        return daoutil.queryScalarList(this, "get_neg_all_not_confirmed_bid_discount_lines", negId);
    }

    @Override
    public void insert(User user, Long negId, Long bidId) {
        Object[] values = {user.getUserId(), user.getUserId(), bidId, negId};
        daoutil.insert(this, values);
    }

    @Override
    public void copyBidDiscounts(User user, Long sourceBidId, Long destinationBidId) {
        Object[] values = {destinationBidId, user.getUserId(), user.getUserId(), sourceBidId};
        daoutil.dml(this, "copy_bid_discounts", values);
    }

    @Override
    public void copyBidActiveDiscounts(User user, Long sourceBidId, Long destinationBidId) {
        Object[] values = {destinationBidId, user.getUserId(), user.getUserId(), sourceBidId};
        daoutil.dml(this, "copy_bid_active_discounts", values);
    }

    @Override
    public void update(Long userId, Long bidId, List<BidDiscount> discounts) {
        List<Object[]> values = discounts.stream().map(this::updateValues).collect(Collectors.toList());
        daoutil.batchUpdate(this, values);
    }

    private Object[] updateValues(BidDiscount discount) {
        return new Object[]{discount.getBoolValue(), discount.getBoolValue(), discount.getNumberValue(),
                discount.getNumberValue(), discount.getLastUpdatedBy(), discount.getBidId(), discount.getBidLineNum(),
                discount.getDiscountId()};
    }

    @Override
    public void updateBidLineTotalDiscount(Long bidId, Integer lineNum, BigDecimal bidLineTotalDiscount) {
        Object[] values = {bidLineTotalDiscount, bidId, lineNum};
        daoutil.dml(this, "update_bid_line_total_discount", values);
    }

    @Override
    public void makeCorrection(List<BidDiscount> discounts) {
        List<Object[]> values = discounts.stream().map(this::correctionValues).collect(Collectors.toList());
        daoutil.batchDML(this, "correction", values);
    }

    private Object[] correctionValues(BidDiscount discount) {
        return new Object[]{discount.getBoolValue(), discount.getNumberValue(), discount.getCorrectionReason(),
                discount.getLastUpdatedBy(), discount.getBidId(), discount.getBidLineNum(), discount.getDiscountId()};
    }

    @Override
    public void confirmBidLineDiscounts(Long userId, Long bidId, Integer lineNum) {
        Object[] values = {userId, bidId, lineNum};
        daoutil.dml(this, "confirm_bid_discount", values);
    }

    @Override
    public void deleteBidDiscounts(Long bidId) {
        Object[] values = {bidId};
        daoutil.delete(this, values);
    }

    @Override
    public void deleteNotParticipatedBidLineDiscounts(Long negId) {
        Object[] param = {negId};
        daoutil.dml(this, "delete_not_participated_bid_line_discounts", param);
    }

    @Override
    public void deleteTender2Stage1NotParticipatedBidLineDiscounts(Long negId) {
        Object[] param = {negId};
        daoutil.dml(this, "delete_tender2_not_participated_bid_line_discounts", param);
    }
}
