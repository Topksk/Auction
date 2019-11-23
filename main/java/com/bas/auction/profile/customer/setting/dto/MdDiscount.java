package com.bas.auction.profile.customer.setting.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MdDiscount extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -705443991749807698L;

    @SerializedName("recid")
    private Long discountId;
    private Long settingId;
    private String negType;
    private String discountCode;
    private String discountType;
    private String description;
    private Boolean isSystem;
    private Boolean applicableForGood;
    private Boolean applicableForWork;
    private Boolean applicableForService;
    private Boolean applicableForStage2;
    private Boolean displayInForm;
    private List<MdDiscountVal> values;

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public long getSettingId() {
        return settingId;
    }

    public void setSettingId(long settingId) {
        this.settingId = settingId;
    }

    public String getNegType() {
        return negType;
    }

    public void setNegType(String negType) {
        this.negType = negType;
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

    public List<MdDiscountVal> getValues() {
        return values;
    }

    public void setValues(List<MdDiscountVal> values) {
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

    public Boolean getDisplayInForm() {
        return displayInForm;
    }

    public void setDisplayInForm(Boolean displayInForm) {
        this.displayInForm = displayInForm;
    }
}
