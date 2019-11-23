package com.bas.auction.neg.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dto.NegLine;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Repository
public class NegLineDAOImpl implements NegLineDAO, GenericDAO<NegLine> {
    private final static Logger logger = LoggerFactory.getLogger(NegLineDAOImpl.class);
    private final SupplierDAO supplierDAO;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegLineDAOImpl(DaoJdbcUtil daoutil, SupplierDAO supplierDAO) {
        this.daoutil = daoutil;
        this.supplierDAO = supplierDAO;
    }

    @Override
    public String getSqlPath() {
        return "negotiations/lines";
    }

    @Override
    public Class<NegLine> getEntityType() {
        return NegLine.class;
    }

    private String findSupplierName(Long supplierId, Map<Long, String> cachedSupplierNames) {
        if (!cachedSupplierNames.containsKey(supplierId)) {
            String supplierName = supplierDAO.findName(supplierId);
            cachedSupplierNames.put(supplierId, supplierName);
        }
        return cachedSupplierNames.get(supplierId);
    }

    private void setNegLineAwardInfo(NegLine line, Map<Long, String> cachedSupplierNames) {
        Long winnerSupplierId = line.getWinnerSupplierId();
        if (winnerSupplierId != null) {
            String supplierName = findSupplierName(winnerSupplierId, cachedSupplierNames);
            line.setWinnerSupplierName(supplierName);
            if (supplierName == null)
                logger.error("Winner supplier name not found: supplierId={}", winnerSupplierId);
        } else if (line.getFailReason() == null)
            line.setAwardReason(null);
    }

    @Override
    public List<NegLine> findNegLines(Long negId) {
        logger.debug("get neg lines: negId={}", negId);
        Map<Long, String> cachedSupplierNames = new HashMap<>();
        List<NegLine> res = daoutil.query(this, "get_neg_lines", negId);
        res.forEach(line -> setNegLineAwardInfo(line, cachedSupplierNames));
        return res;
    }

    @Override
    public List<NegLine> findBidNegLines(Long negId, Long bidId) {
        logger.debug("get neg lines: negId={}, bidId={}", negId, bidId);
        Map<Long, String> cachedSupplierNames = new HashMap<>();
        List<NegLine> res = daoutil.query(this, "get_bid_neg_lines", negId, bidId);
        res.forEach(line -> setNegLineAwardInfo(line, cachedSupplierNames));
        return res;
    }

    @Override
    public Map<Integer, BigDecimal> findNegLineUnitPrices(Long negId) {
        logger.debug("get neg lines: negId={}", negId);
        List<Map<String, Object>> negLineBidPrices = daoutil.queryForTypedMapList(this, "get_neg_line_unit_prices", negId);
        return negLineBidPrices.stream()
                .map(this::mapLineNumToUnitPrice)
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Integer, BigDecimal> mapLineNumToUnitPrice(Map<String, Object> m) {
        return new SimpleEntry<>((Integer) m.get("line_num"), (BigDecimal) m.get("unit_price"));
    }

    @Override
    public Boolean findIsDiscountConfirmed(Long bidId, Integer lineNum) {
        logger.debug("discount confirmed: bidId = {}, lineNum = {}", bidId, lineNum);
        return daoutil.queryScalar(this, "discounts_confirmed", bidId, lineNum);
    }

    private boolean exists(Long negId, Long planId) {
        return daoutil.exists(this, "exists", negId, planId);
    }

    private int findMaxLineNum(Long negId) {
        return daoutil.queryScalar(this, "max_line_num", negId);
    }

    @Override
    public List<Integer> findNegLineNums(Long negId) {
        return daoutil.queryScalarList(this, "get_neg_line_nums", negId);
    }

    @Override
    public List<Integer> findNotFailedNegLineNums(Long negId) {
        return daoutil.queryScalarList(this, "get_not_failed_neg_line_nums", negId);
    }

    @Override
    public BigDecimal findNegLinesMinUnitPrice(Long negId) {
        return daoutil.queryScalar(this, "get_neg_lines_min_unit_price", negId);
    }

    @Override
    public Map<String, Long> findNegLineTotalAndPermittedBidCounts(Long negId, Integer lineNum) {
        return daoutil.queryForTypedMap(this, "neg_line_total_and_permitted_bid_count", negId, lineNum);
    }

    @Override
    public List<Map<String, Long>> findPriceBasedNegLineBidRanks(Long negId, Integer lineNum) {
        return daoutil.queryForTypedMapList(this, "price_based_neg_line_bid_ranks", negId, lineNum);
    }

    @Override
    public List<Map<String, Long>> findDiscountedPriceBasedNegLineBidRanks(Long negId, Integer lineNum) {
        return daoutil.queryForTypedMapList(this, "discounted_price_based_neg_line_bid_ranks", negId, lineNum);
    }

    @Override
    public String findNegLinePurchaseType(Long negId, Integer lineNum) {
        return daoutil.queryScalar(this, "get_purchase_method", negId, lineNum);
    }

    @Override
    @SpringTransactional
    public void create(User user, Long negId, List<Long> planIds) {
        if (planIds == null)
            return;
        logger.debug("create neg lines: negId = {}, plans = {}", negId, planIds);
        List<Object[]> values = new ArrayList<>();
        int lineNum = findMaxLineNum(negId);
        for (Long id : planIds) {
            if (exists(negId, id))
                continue;
            lineNum++;
            Object[] vals = {negId, lineNum, user.getUserId(), user.getUserId(), id};
            values.add(vals);
        }
        daoutil.batchInsert(this, values);
    }

    @Override
    @SpringTransactional
    public void copyNotFailedNegLines(User user, Long sourceNegId, Long destinationNegId) {
        Object[] values = {destinationNegId, user.getUserId(), user.getUserId(), sourceNegId};
        daoutil.dml(this, "copy_not_failed_lines", values);
    }

    @Override
    @SpringTransactional
    public void delete(Long negId, List<Long> planIds) {
        if (planIds == null)
            return;
        logger.debug("delete neg lines: negId = {}, lines = {}", negId, planIds);
        List<Object[]> values = planIds.stream()
                .map(id -> mapToDeleteValues(negId, id))
                .collect(toList());
        daoutil.batchDelete(this, values);
        renumberLines(negId);
    }

    private Object[] mapToDeleteValues(Long negId, Long id) {
        return new Object[]{negId, id};
    }

    protected void renumberLines(Long negId) {
        List<Integer> negLineNums = findNegLineNums(negId);
        if (negLineNums.isEmpty())
            return;
        logger.debug("renumber neg lines: negId = {}, lines = {}", negId, negLineNums);
        Collections.sort(negLineNums);
        Collections.reverse(negLineNums);
        List<Integer> newLineNums = IntStream.rangeClosed(1, negLineNums.size()).boxed().collect(toList());
        List<Object[]> values = new ArrayList<>(newLineNums.size());
        for (Integer i : newLineNums) {
            Integer oldLine;
            if (negLineNums.contains(i)) {
                oldLine = i;
                negLineNums.remove(i);
            } else {
                oldLine = negLineNums.remove(0);
            }
            values.add(new Object[]{i, negId, oldLine});
        }
        daoutil.batchDML(this, "renumber_lines", values);
    }

    @Override
    @SpringTransactional
    public void deleteNegLines(Long negId) {
        logger.debug("delete all neg lines: negId = {}", negId);
        daoutil.dml(this, "delete_neg_lines", new Object[]{negId});
    }

    @Override
    @SpringTransactional
    public void finalizeNegLines(List<NegLine> negLines) {
        logger.debug("finalize neg lines");
        List<Object[]> params = negLines.stream()
                .map(this::getAwardLineUpdateParams)
                .collect(toList());
        daoutil.batchDML(this, "finalize_neg_lines", params);
    }

    private Object[] getAwardLineUpdateParams(NegLine negLine) {
        return new Object[]{negLine.getWinnerSupplierId(), negLine.getFailReason(), negLine.getLastUpdatedBy(),
                negLine.getNegId(), negLine.getLineNum()};
    }

    @Override
    @SpringTransactional
    public void resetAwards(Long userId, Long negId) {
        logger.debug("reset neg line awards: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "reset_awards", values);
    }

    @Override
    @SpringTransactional
    public void updateBidCount(Long negId) {
        logger.debug("update neg line bid count: negId={}", negId);
        Object[] values = {negId, negId};
        daoutil.dml(this, "update_neg_line_bid_count", values);
    }

    @Override
    public List<Map<String, Object>> findForIntegra(long negId) {
        return daoutil.queryForMapList(this, "list_for_integra", negId);
    }

    @Override
    @SpringTransactional
    public void confirmNegLineDiscounts(Long userId, Long negId, Integer lineNum) {
        Object[] values = {userId, negId, lineNum};
        daoutil.dml(this, "confirm_neg_line_discounts", values);
    }
}
