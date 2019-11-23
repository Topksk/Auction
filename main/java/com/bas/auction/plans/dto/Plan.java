package com.bas.auction.plans.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class Plan extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -7572985735910553827L;
    @SerializedName("recid")
    private long planId;
    private String planNumber;
    private long orgId;
    private long customerId;
    private int financialYear;
    private String itemCode;
    private String itemCodeDesc;
    private String itemNameRu;
    private String itemNameKz;
    private String itemShortDescRu;
    private String itemShortDescKz;
    private String itemLongDescRu;
    private String itemLongDescKz;
    private String purchaseType;
    private String purchaseMethod;
    private BigDecimal kzContent;
    private String purchaseLocationKato;
    private String purchaseLocation;
    private String shippingLocation;
    private String incoterms2010;
    private String shippingDate;
    private String uomCode;
    @ExcludeFromSearchIndex
    private String uomMeasure;
    private BigDecimal quantity;
    private BigDecimal amountWithoutVat;
    private BigDecimal amountWithVat;
    private BigDecimal unitPrice;
    private String purchasePriority;
    private String note;
    private String status;
    private String shippingRegion;
    private String purchasePeriod;
    private String prepayment;
    private long settingId;
    private String itemCodeListType;

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }

    public String getPlanNumber() {
        return planNumber;
    }

    public void setPlanNumber(String planNumber) {
        this.planNumber = planNumber;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public int getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(int financialYear) {
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

    public String getPurchaseMethod() {
        return purchaseMethod;
    }

    public void setPurchaseMethod(String purchaseMethod) {
        this.purchaseMethod = purchaseMethod;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAmountWithoutVat() {
        return amountWithoutVat;
    }

    public void setAmountWithoutVat(BigDecimal amountWithoutVat) {
        this.amountWithoutVat = amountWithoutVat;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getItemCodeDesc() {
        return itemCodeDesc;
    }

    public void setItemCodeDesc(String itemCodeDesc) {
        this.itemCodeDesc = itemCodeDesc;
    }

    public String getUomMeasure() {
        return uomMeasure;
    }

    public void setUomMeasure(String uomMeasure) {
        this.uomMeasure = uomMeasure;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getShippingRegion() {
        return shippingRegion;
    }

    public void setShippingRegion(String shippingRegion) {
        this.shippingRegion = shippingRegion;
    }

    public String getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(String prepayment) {
        this.prepayment = prepayment;
    }

    public String getPurchasePeriod() {
        return purchasePeriod;
    }

    public void setPurchasePeriod(String purchasePeriod) {
        this.purchasePeriod = purchasePeriod;
    }

    public String getItemLongDescRu() {
        return itemLongDescRu;
    }

    public void setItemLongDescRu(String itemLongDescRu) {
        this.itemLongDescRu = itemLongDescRu;
    }

    public String getItemLongDescKz() {
        return itemLongDescKz;
    }

    public void setItemLongDescKz(String itemLongDesckz) {
        this.itemLongDescKz = itemLongDesckz;
    }

    public long getSettingId() {
        return settingId;
    }

    public void setSettingId(long settingId) {
        this.settingId = settingId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (planId ^ (planId >>> 32));
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
        Plan other = (Plan) obj;
        if (planId != other.planId)
            return false;
        return true;
    }

    public BigDecimal getKzContent() {
        return kzContent;
    }

    public void setKzContent(BigDecimal kzContent) {
        this.kzContent = kzContent;
    }

    public String getPurchaseLocationKato() {
        return purchaseLocationKato;
    }

    public void setPurchaseLocationKato(String purchaseLocationKato) {
        this.purchaseLocationKato = purchaseLocationKato;
    }

    public String getPurchaseLocation() {
        return purchaseLocation;
    }

    public void setPurchaseLocation(String purchaseLocation) {
        this.purchaseLocation = purchaseLocation;
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

    public String getItemCodeListType() {
        return itemCodeListType;
    }

    public void setItemCodeListType(String itemCodeListType) {
        this.itemCodeListType = itemCodeListType;
    }
}
