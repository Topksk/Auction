package com.bas.auction.neg.dto;

import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class NegLine extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = 1383817207852966072L;
    @ExcludeFromSearchIndex
    private long negId;
    @SerializedName("recid")
    private Long planId;
    private String planNumber;
    private int lineNum;
    private BigDecimal quantity;
    private String uomCode;
    private String uomDesc;
    private Integer financialYear;
    private String itemCode;
    private String itemCodeDesc;
    private String itemNameRu;
    private String itemNameKz;
    private String itemShortDescRu;
    private String itemShortDescKz;
    private String itemLongDescRu;
    private String itemLongDescKz;
    private String purchaseType;
    private BigDecimal kzContent;
    private String purchaseLocation;
    private String purchaseLocationKato;
    private String shippingLocation;
    private String incoterms2010;
    private String shippingDate;
    private String purchasePeriod;
    private BigDecimal amountWithoutVat;
    private BigDecimal amountWithVat;
    private BigDecimal unitPrice;

    @ExcludeFromSearchIndex
    private BigDecimal bestPrice;

    private String purchasePriority;
    private String note;
    private Long winnerSupplierId;
    private Integer bidCount;
    private String winnerSupplierName;
    private String failReason;
    private String awardReason;
    @ExcludeFromSearchIndex
    private List<BidLinePermissions> bidLinePermissions;
    private String prepayment;
    private String shippingRegion;
    private Boolean discountsConfirmed;

    public long getNegId() {
        return negId;
    }

    public void setNegId(long negId) {
        this.negId = negId;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getUomDesc() {
        return uomDesc;
    }

    public void setUomDesc(String uomDesc) {
        this.uomDesc = uomDesc;
    }

    public Long getWinnerSupplierId() {
        return winnerSupplierId;
    }

    public void setWinnerSupplierId(Long winnerSupplierId) {
        this.winnerSupplierId = winnerSupplierId;
    }

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(Integer bidCount) {
        this.bidCount = bidCount;
    }

    public String getWinnerSupplierName() {
        return winnerSupplierName;
    }

    public void setWinnerSupplierName(String winnerSupplierName) {
        this.winnerSupplierName = winnerSupplierName;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getAwardReason() {
        return awardReason;
    }

    public void setAwardReason(String awardReason) {
        this.awardReason = awardReason;
    }

    public boolean isAwarded() {
        return failReason == null && winnerSupplierId != null;
    }

    public boolean isFailed() {
        return failReason != null;
    }

    public int getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(Integer financialYear) {
        this.financialYear = financialYear;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemNameRu() {
        return itemNameRu;
    }

    public void setItemNameRu(String itemNameRu) {
        this.itemNameRu = itemNameRu;
    }

    public String getItemNameKz() {
        return itemNameKz;
    }

    public void setItemNameKz(String itemNameKz) {
        this.itemNameKz = itemNameKz;
    }

    public String getItemShortDescRu() {
        return itemShortDescRu;
    }

    public void setItemShortDescRu(String itemShortDescRu) {
        this.itemShortDescRu = itemShortDescRu;
    }

    public String getItemShortDescKz() {
        return itemShortDescKz;
    }

    public void setItemShortDescKz(String itemShortDescKz) {
        this.itemShortDescKz = itemShortDescKz;
    }

    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getShippingLocation() {
        return shippingLocation;
    }

    public void setShippingLocation(String shippingLocation) {
        this.shippingLocation = shippingLocation;
    }

    public String getIncoterms2010() {
        return incoterms2010;
    }

    public void setIncoterms2010(String incoterms2010) {
        this.incoterms2010 = incoterms2010;
    }

    public String getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(String shippingDate) {
        this.shippingDate = shippingDate;
    }

    public BigDecimal getAmountWithoutVat() {
        return amountWithoutVat;
    }

    public void setAmountWithoutVat(BigDecimal amountWithoutVat) {
        this.amountWithoutVat = amountWithoutVat;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<BidLinePermissions> getBidLinePermissions() {
        return bidLinePermissions;
    }

    public void setBidLinePermissions(List<BidLinePermissions> bidBermissions) {
        this.bidLinePermissions = bidBermissions;
    }

    public String getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(String prepayment) {
        this.prepayment = prepayment;
    }

    public String getShippingRegion() {
        return shippingRegion;
    }

    public void setShippingRegion(String shippingRegion) {
        this.shippingRegion = shippingRegion;
    }

    public String getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(String planNumber) {
        this.planNumber = planNumber;
    }

    public Boolean getDiscountsConfirmed() {
        return discountsConfirmed;
    }

    public void setDiscountsConfirmed(Boolean discountsConfirmed) {
        this.discountsConfirmed = discountsConfirmed;
    }

    public String getItemCodeDesc() {
        return itemCodeDesc;
    }

    public void setItemCodeDesc(String itemCodeDesc) {
        this.itemCodeDesc = itemCodeDesc;
    }

    public String getPurchasePeriod() {
        return purchasePeriod;
    }

    public void setPurchasePeriod(String purchasePeriod) {
        this.purchasePeriod = purchasePeriod;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lineNum;
        result = prime * result + (int) (negId ^ (negId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NegLine other = (NegLine) obj;
        return lineNum == other.lineNum && negId == other.negId;
    }

    public String getPurchaseLocation() {
        return purchaseLocation;
    }

    public void setPurchaseLocation(String purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
    }

    public String getPurchaseLocationKato() {
        return purchaseLocationKato;
    }

    public void setPurchaseLocationKato(String purchaseLocationKato) {
        this.purchaseLocationKato = purchaseLocationKato;
    }

    public String getItemLongDescRu() {
        return itemLongDescRu;
    }

    public BigDecimal getBestPrice() {
        return bestPrice;
    }

    public NegLine setBestPrice(BigDecimal bestPrice) {
        this.bestPrice = bestPrice;
        return this;
    }

    public void setItemLongDescRu(String itemLongDescRu) {
        this.itemLongDescRu = itemLongDescRu;
    }

    public String getItemLongDescKz() {
        return itemLongDescKz;
    }

    public void setItemLongDescKz(String itemLongDescKz) {
        this.itemLongDescKz = itemLongDescKz;
    }

    public BigDecimal getKzContent() {
        return kzContent;
    }

    public void setKzContent(BigDecimal kzContent) {
        this.kzContent = kzContent;
    }

    public BigDecimal getAmountWithVat() {
        return amountWithVat;
    }

    public void setAmountWithVat(BigDecimal amountWithVat) {
        this.amountWithVat = amountWithVat;
    }

    public String getPurchasePriority() {
        return purchasePriority;
    }

    public void setPurchasePriority(String purchasePriority) {
        this.purchasePriority = purchasePriority;
    }

}
