package com.bas.auction.bid.discount.dto;

import java.math.BigDecimal;

import com.bas.auction.core.dto.AuditableRow;

public class BidDiscount extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7852948067880681609L;

	private long bidId;
	private int bidLineNum;
	private long discountId;
	private Boolean boolValue;
	private Boolean boolValueOrig;
	private BigDecimal numberValue;
	private BigDecimal numberValueOrig;
	private String correctionReason;

	public long getBidId() {
		return bidId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
	}

	public int getBidLineNum() {
		return bidLineNum;
	}

	public void setBidLineNum(int bidLineNum) {
		this.bidLineNum = bidLineNum;
	}

	public long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(long discountId) {
		this.discountId = discountId;
	}

	public BigDecimal getNumberValue() {
		return numberValue;
	}

	public void setNumberValue(BigDecimal numberValue) {
		this.numberValue = numberValue;
	}

	public Boolean getBoolValue() {
		return boolValue;
	}

	public void setBoolValue(Boolean boolValue) {
		this.boolValue = boolValue;
	}

	public Boolean getBoolValueOrig() {
		return boolValueOrig;
	}

	public void setBoolValueOrig(Boolean boolValueOrig) {
		this.boolValueOrig = boolValueOrig;
	}

	public BigDecimal getNumberValueOrig() {
		return numberValueOrig;
	}

	public void setNumberValueOrig(BigDecimal numberValueOrig) {
		this.numberValueOrig = numberValueOrig;
	}

	public String getCorrectionReason() {
		return correctionReason;
	}

	public void setCorrectionReason(String correctionReason) {
		this.correctionReason = correctionReason;
	}

}
