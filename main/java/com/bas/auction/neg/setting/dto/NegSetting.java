package com.bas.auction.neg.setting.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class NegSetting extends AuditableRow {
    @SerializedName("recid")
    private long negId;
    private String awardMethod;
    private boolean foreignCurrencyControl;
    private Integer auctionDuration;
    private Integer auctionExtTimeLeft;
    private Integer auctionExtDuration;
    private Integer auctionExtNumber;

    public long getNegId() {
        return negId;
    }

    public void setNegId(long negId) {
        this.negId = negId;
    }

    public String getAwardMethod() {
        return awardMethod;
    }

    public void setAwardMethod(String awardMethod) {
        this.awardMethod = awardMethod;
    }

    public boolean isForeignCurrencyControl() {
        return foreignCurrencyControl;
    }

    public void setForeignCurrencyControl(boolean foreignCurrencyControl) {
        this.foreignCurrencyControl = foreignCurrencyControl;
    }

    public Integer getAuctionDuration() {
        return auctionDuration;
    }

    public void setAuctionDuration(Integer auctionDuration) {
        this.auctionDuration = auctionDuration;
    }

    public Integer getAuctionExtTimeLeft() {
        return auctionExtTimeLeft;
    }

    public void setAuctionExtTimeLeft(Integer auctionExtTimeLeft) {
        this.auctionExtTimeLeft = auctionExtTimeLeft;
    }

    public Integer getAuctionExtDuration() {
        return auctionExtDuration;
    }

    public void setAuctionExtDuration(Integer auctionExtDuration) {
        this.auctionExtDuration = auctionExtDuration;
    }

    public Integer getAuctionExtNumber() {
        return auctionExtNumber;
    }

    public void setAuctionExtNumber(Integer auctionExtNumber) {
        this.auctionExtNumber = auctionExtNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NegSetting that = (NegSetting) o;

        return negId == that.negId;

    }

    @Override
    public int hashCode() {
        return (int) (negId ^ (negId >>> 32));
    }
}
