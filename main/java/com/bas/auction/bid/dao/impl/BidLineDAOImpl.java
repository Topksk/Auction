package com.bas.auction.bid.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Repository
public class BidLineDAOImpl implements BidLineDAO, GenericDAO<BidLine> {
    private final static Logger logger = LoggerFactory.getLogger(BidLineDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public BidLineDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "bids/lines";
    }

    @Override
    public Class<BidLine> getEntityType() {
        return BidLine.class;
    }

    @Override
    public List<BidLine> findBidLines(Long bidId) {
        return daoutil.query(this, "get", bidId);
    }

    @Override
    public Map<Integer, BigDecimal> findBidLinePrices(Long bidId) {
        List<Map<String, Object>> result = daoutil.queryForMapList(this, "get_bid_line_prices", bidId);
        Map<Integer, BigDecimal> bidLinePrices = new LinkedHashMap<>();
        result.forEach(map -> bidLinePrices.put((Integer) map.get("line_num"), (BigDecimal) map.get("bid_price")));
        return bidLinePrices;
    }

    @Override
    public void insert(User user, Long negId, Long bidId) {
        Object[] values = {bidId, "DRAFT", user.getUserId(), user.getUserId(), negId};
        daoutil.insert(this, values);
    }

    @Override
    public void copyBidLinesForReplace(User user, Long sourceBidId, Long destinationBidId) {
        Object[] values = {destinationBidId, user.getUserId(), user.getUserId(), sourceBidId};
        daoutil.dml(this, "copy_bid_lines_for_replace", values);
    }

    @Override
    public void copyBidLinesForTender2Stage2(User user, Long sourceBidId, Long destinationBidId) {
        Object[] values = {destinationBidId, "DRAFT", user.getUserId(), user.getUserId(), sourceBidId};
        daoutil.dml(this, "copy_bid_lines_for_tender2_stage2", values);
    }

    @Override
    public void updateStatuses(Long userId, Long bidId, String status) {
        Object[] values = {status, userId, bidId};
        daoutil.dml(this, "update_bid_lines_statuses", values);
    }

    @Override
    public void updateParticipatingBidLinesStatuses(Long userId, Long bidId, String status) {
        Object[] values = {status, userId, bidId};
        daoutil.dml(this, "update_participating_bid_lines_statuses", values);
    }

    @Override
    public void updateBidLineTotalDiscount(List<BidLine> bidLines) {
        List<Object[]> values = bidLines.stream().map(this::updateForPublishValues).collect(toList());
        daoutil.batchDML(this, "update_bid_line_discount", values);
    }

    private Object[] updateForPublishValues(BidLine line) {
        return new Object[]{line.getDiscount(), line.getDiscountConfirmed(), line.getBidId(), line.getLineNum()};
    }

    @Override
    public void update(List<BidLine> bidLines) {
        List<Object[]> values = bidLines.stream().map(this::updateValues).collect(toList());
        daoutil.batchUpdate(this, values);
    }

    private Object[] updateValues(BidLine line) {
        return new Object[]{line.getBidPrice(), line.getParticipateTender2(), line.getLastUpdatedBy(),
                line.getBidId(), line.getLineNum()};
    }

    @Override
    public void deleteBidLines(Long bidId) {
        Object[] values = {bidId};
        daoutil.delete(this, values);
    }

    @Override
    public void deleteNotParticipatedBidLines(Long negId) {
        Object[] param = {negId};
        daoutil.dml(this, "delete_not_participated_bid_lines", param);
    }

    @Override
    public void deleteTender2Stage1NotParticipatedBidLines(Long negId) {
        Object[] param = {negId};
        daoutil.dml(this, "delete_tender2_not_participated_bid_lines", param);
    }

    @Override
    public void failBidLines(Long negId, List<BidLine> failedBidLines) {
        logger.debug("fail bid lines");
        List<Object[]> params = failedBidLines.stream()
                .map(bl -> getFailedBidLineUpdateParams(negId, bl))
                .collect(toList());
        daoutil.batchDML(this, "fail_bid_lines", params);
    }

    @Override
    public void updateRejectedBidLineStatuses(Long negId) {
        logger.debug("update rejected bid line statuses");
        Object[] values = {negId};
        daoutil.dml(this, "update_rejected_bid_line_statuses", values);
    }

    private Object[] getFailedBidLineUpdateParams(Long negId, BidLine bidLine) {
        return new Object[]{bidLine.getBidLineStatus(), bidLine.getLastUpdatedBy(), negId, bidLine.getLineNum()};
    }

    @Override
    public void rankBidLines(List<BidLine> rankedBidLines) {
        logger.debug("rank neg lines");
        List<Object[]> params = rankedBidLines.stream().map(this::getRankedBidLineUpdateParams).collect(toList());
        daoutil.batchDML(this, "rank_bid_line", params);
    }

    private Object[] getRankedBidLineUpdateParams(BidLine bidLine) {
        return new Object[]{bidLine.getBidLineStatus(), bidLine.getRank(), bidLine.getLastUpdatedBy(),
                bidLine.getBidId(), bidLine.getLineNum()};
    }

    @Override
    public void resetAwardStatusesAndRanks(Long userId, Long negId) {
        Object[] values = {userId, negId};
        daoutil.dml(this, "reset_award_statuses_and_ranks", values);
    }

}
