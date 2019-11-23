package com.bas.auction.neg.setting.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NegDiscount extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -705443991749807698L;
    private long negId;
    @SerializedName("recid")
    private Long discountId;
    private String discountCode;
    private String discountType;
    private String description;
    private Boolean isSystem;
    private Boolean applicableForGood;
    private Boolean applicableForWork;
    private Boolean applicableForService;
    private Boolean applicableForStage2;
    private Boolean displayInForm;
    private List<NegDiscountVal> values;

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public List<NegDiscountVal> getValues() {
        return values;
    }

    public void setValues(List<NegDiscountVal> values) {
        this.values = values;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Boolean isApplicableForGood() {
        return applicableForGood;
    }

    public void setApplicableForGood(Boolean applicableForGood) {
        this.applicableForGood = applicableForGood;
    }

    public Boolean isApplicableForWork() {
        return applicableForWork;
    }

    public void setApplicableForWork(Boolean applicableForWork) {
        this.applicableForWork = applicableForWork;
    }

    public Boolean isApplicableForService() {
        return applicableForService;
    }

    public void setApplicableForService(Boolean applicableForService) {
        this.applicableForService = applicableForService;
    }

    public void setApplicableForStage2(Boolean applicableForStage2) {
        this.applicableForStage2 = applicableForStage2;
    }

    public Boolean isApplicableForStage2() {
        return applicableForStage2;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public long getNegId() {
        return negId;
    }

    public void setNegId(long negId) {
        this.negId = negId;
    }

    public Boolean getDisplayInForm() {
        return displayInForm != null && displayInForm;
    }

    public void setDisplayInForm(Boolean displayInForm) {
        this.displayInForm = displayInForm;
    }
}
