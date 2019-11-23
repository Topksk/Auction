package com.bas.auction.profile.customer.setting.dto;

import java.math.BigDecimal;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class MdDiscountVal extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3470749608921448831L;

	@SerializedName("recid")
	private long discountValId;
	private long discountId;
	private Boolean boolValue;
	private BigDecimal numberFrom;
	private BigDecimal numberTo;
	private BigDecimal discount;

	public long getDiscountValId() {
		return discountValId;
	}

	public void setDiscountValId(long discountValId) {
		this.discountValId = discountValId;
	}

	public long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(long discountId) {
		this.discountId = discountId;
	}

	public BigDecimal getNumberFrom() {
		return numberFrom;
	}

	public void setNumberFrom(BigDecimal numberFrom) {
		this.numberFrom = numberFrom;
	}

	public BigDecimal getNumberTo() {
		return numberTo;
	}

	public void setNumberTo(BigDecimal numberTo) {
		this.numberTo = numberTo;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public Boolean getBoolValue() {
		return boolValue;
	}

	public void setBoolValue(Boolean boolValue) {
		this.boolValue = boolValue;
	}

}
