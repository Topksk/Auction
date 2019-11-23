package com.bas.auction.currency.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class ExchangeRate extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1452378735544830022L;

	private String fromCurrency;
	@SerializedName("currency")
	private String toCurrency;
	private Date activeDate;
	private Integer quant;
	private BigDecimal rate;
	private String fromName;
	@SerializedName("name")
	private String toName;

	public String getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}

	public Date getActiveDate() {
		return activeDate;
	}

	public void setActiveDate(Date activeDate) {
		this.activeDate = activeDate;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Integer getQuant() {
		return quant;
	}

	public void setQuant(Integer quant) {
		this.quant = quant;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

}
