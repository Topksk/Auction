package com.bas.auction.auth.dto;

public class UserAuthInfo extends User {

    public enum RegStatus {
        APPROVED, AGREEMENT_NOT_SIGNED, NOT_REGISTERED_USER, IN_PROGRESS, REJECTED, IN_PROGRESS_CUSTOMER, NOT_ACTIVE_CUSTOMER_USER,
        NOT_APPROVED_SUPPLIER, EMAIL_NOT_ACTIVE_SUPPLIER_USER, IN_PROGRESS_SUPPLIER, SENT_FOR_APPROVAL,
        REJECTED_SUPPLIER, NOT_ACTIVE_SUPPLIER_USER
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Boolean emailActivated;
    private String regStatus;
    private Boolean nonresident;

    public Boolean getEmailActivated() {
        return emailActivated != null && emailActivated;
    }

    public void setEmailActivated(Boolean emailActivated) {
        this.emailActivated = emailActivated;
    }

    public String getRegStatus() {
        return regStatus;
    }

    public RegStatus getRegStatusEnum() {
        if (regStatus == null)
            return null;
        return RegStatus.valueOf(regStatus);
    }

    public void setRegStatus(String regStatus) {
        this.regStatus = regStatus;
    }

    public void setRegStatusEnum(RegStatus regStatus) {
        if (regStatus == null)
            this.regStatus = null;
        else
            this.regStatus = regStatus.toString();
    }

    public Boolean getNonresident() {
        return nonresident;
    }

    public void setNonresident(Boolean nonresident) {
        this.nonresident = nonresident;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
