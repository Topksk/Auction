package com.bas.auction.bid.permission.service.impl;

import com.bas.auction.bid.permission.dto.BidLinePermissionDetails;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.neg.dto.Negotiation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

public class BidDumpingPermissionControl {
    public final static String RELATED_TO_PLAN_PRICE = "RELATED_TO_PLAN_PRICE";
    public final static String RELATED_TO_MEAN = "RELATED_TO_MEAN";
    private final Long requirementId;
    private final String rejectReason;
    private final Negotiation neg;
    private final boolean goodDumpingControlEnabled;
    private final boolean workDumpingControlEnabled;
    private final boolean serviceDumpingControlEnabled;
    private BigDecimal goodDumpingThresholdCoeff;
    private BigDecimal workDumpingThresholdCoeff;
    private BigDecimal serviceDumpingThresholdCoeff;

    public BidDumpingPermissionControl(Negotiation neg, Long requirementId, String rejectReason) {
        this.neg = neg;
        this.requirementId = requirementId;
        this.rejectReason = rejectReason;
        this.goodDumpingControlEnabled = neg.getGoodDumpingCalcMethod() != null;
        this.workDumpingControlEnabled = neg.getWorkDumpingCalcMethod() != null;
        this.serviceDumpingControlEnabled = neg.getServiceDumpingCalcMethod() != null;
        initThresholdCoeffs();
    }

    public BidLinePermissions performBidLineDumpingControl(Map<String, Object> linePrices) {
        Long bidId = (Long) linePrices.get("bid_id");
        Integer bidLineNum = (Integer) linePrices.get("line_num");
        boolean permit = isPermittedBidLinePrice(linePrices);
        String reason = permit ? null : rejectReason;
        BidLinePermissionDetails linePermission = new BidLinePermissionDetails();
        linePermission.setRequirementId(requirementId);
        linePermission.setPermitted(permit);
        linePermission.setRejectReason(reason);
        BidLinePermissions permission = new BidLinePermissions();
        permission.setBidId(bidId);
        permission.setBidLineNum(bidLineNum);
        permission.setPermissions(Collections.singletonList(linePermission));
        return permission;
    }

    private boolean isPermittedBidLinePrice(Map<String, Object> linePricesInfo) {
        boolean isGood = isGood(linePricesInfo);
        boolean isWork = isWork(linePricesInfo);
        boolean isService = isService(linePricesInfo);
        if (isGood && !goodDumpingControlEnabled)
            return true;
        if (isWork && !workDumpingControlEnabled)
            return true;
        if (isService && !serviceDumpingControlEnabled)
            return true;
        if (isGood)
            return isPermittedGoodBidLinePrice(linePricesInfo);
        if (isWork)
            return isPermittedWorkBidLinePrice(linePricesInfo);
        if (isService)
            return isPermittedServiceBidLinePrice(linePricesInfo);
        throw new IllegalArgumentException("Illegal dumping calculation method");
    }

    private boolean isPermittedGoodBidLinePrice(Map<String, Object> linePricesInfo) {
        String calcMethod = neg.getGoodDumpingCalcMethod();
        BigDecimal comparePrice = getComparePriceForBidLine(calcMethod, linePricesInfo);
        BigDecimal bidLinePrice = getBidLinePrice(linePricesInfo);
        return isPermittedBidPrice(comparePrice, bidLinePrice, goodDumpingThresholdCoeff);
    }

    private boolean isPermittedWorkBidLinePrice(Map<String, Object> linePricesInfo) {
        String calcMethod = neg.getWorkDumpingCalcMethod();
        BigDecimal comparePrice = getComparePriceForBidLine(calcMethod, linePricesInfo);
        BigDecimal bidLinePrice = getBidLinePrice(linePricesInfo);
        return isPermittedBidPrice(comparePrice, bidLinePrice, workDumpingThresholdCoeff);
    }

    private boolean isPermittedServiceBidLinePrice(Map<String, Object> linePricesInfo) {
        String calcMethod = neg.getServiceDumpingCalcMethod();
        BigDecimal comparePrice = getComparePriceForBidLine(calcMethod, linePricesInfo);
        BigDecimal bidLinePrice = getBidLinePrice(linePricesInfo);
        return isPermittedBidPrice(comparePrice, bidLinePrice, serviceDumpingThresholdCoeff);
    }

    private boolean isPermittedBidPrice(BigDecimal comparePrice, BigDecimal bidLinePrice,
                                        BigDecimal dumpingThresholdCoeff) {
        BigDecimal dumpingThreshold = comparePrice.multiply(dumpingThresholdCoeff).setScale(2, RoundingMode.DOWN);
        return bidLinePrice.compareTo(dumpingThreshold) >= 0;
    }

    private boolean isGood(Map<String, Object> linePricesInfo) {
        return "GOOD".equals(linePricesInfo.get("purchase_type"));
    }

    private boolean isWork(Map<String, Object> linePricesInfo) {
        return "WORK".equals(linePricesInfo.get("purchase_type"));
    }

    private boolean isService(Map<String, Object> linePricesInfo) {
        return "SERVICE".equals(linePricesInfo.get("purchase_type"));
    }

    private BigDecimal getComparePriceForBidLine(String calcMethod, Map<String, Object> linePricesInfo) {
        if (RELATED_TO_PLAN_PRICE.equals(calcMethod))
            return getNegLineUnitPrice(linePricesInfo);
        else if (RELATED_TO_MEAN.equals(calcMethod))
            return getBidsMeanPrice(linePricesInfo);
        throw new IllegalArgumentException("Illegal dumping calculation method");
    }

    private BigDecimal getNegLineUnitPrice(Map<String, Object> linePricesInfo) {
        return (BigDecimal) linePricesInfo.get("unit_price");
    }

    private BigDecimal getBidLinePrice(Map<String, Object> linePricesInfo) {
        return (BigDecimal) linePricesInfo.get("bid_price");
    }

    private BigDecimal getBidsMeanPrice(Map<String, Object> linePricesInfo) {
        return (BigDecimal) linePricesInfo.get("mean_price");
    }

    private void initThresholdCoeffs() {
        if (goodDumpingControlEnabled)
            goodDumpingThresholdCoeff = calcThresholdCoeff(neg.getGoodDumpingThreshold());
        if (workDumpingControlEnabled)
            workDumpingThresholdCoeff = calcThresholdCoeff(neg.getWorkDumpingThreshold());
        if (serviceDumpingControlEnabled)
            serviceDumpingThresholdCoeff = calcThresholdCoeff(neg.getServiceDumpingThreshold());
    }

    private BigDecimal calcThresholdCoeff(BigDecimal dumpingThreshold) {
        BigDecimal HUNDRED = new BigDecimal(100);
        BigDecimal thresholdPercent = dumpingThreshold.divide(HUNDRED, 2, RoundingMode.DOWN);
        return BigDecimal.ONE.subtract(thresholdPercent);
    }
}
