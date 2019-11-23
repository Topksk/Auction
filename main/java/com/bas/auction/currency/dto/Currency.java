package com.bas.auction.currency.dto;

import com.google.gson.annotations.SerializedName;

public class Currency {
	@SerializedName("recid")
	private String code;
	private String name;
	private boolean active;
	private boolean functionalCurrency;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isFunctionalCurrency() {
		return functionalCurrency;
	}

	public void setFunctionalCurrency(boolean functionalCurrency) {
		this.functionalCurrency = functionalCurrency;
	}

}
