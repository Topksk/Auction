package com.bas.auction.lookup.dto;

public class UnitOfMeasure implements HasCode {
	private String code;
	private String measure;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}
}
