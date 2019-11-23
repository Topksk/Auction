package com.bas.auction.billing.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class BillPlan extends AuditableRow {
    @SerializedName("recid")
    private Long billPlanId;
    private String planType;
    private Long customerId;
    private Long supplierId;
    private Date effectiveFrom;
    private Date effectiveTo;

    public Long getBillPlanId() {
        return billPlanId;
    }

    public void setBillPlanId(Long billPlanId) {
        this.billPlanId = billPlanId;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Date effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
}
