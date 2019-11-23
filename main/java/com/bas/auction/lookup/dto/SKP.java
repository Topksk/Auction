package com.bas.auction.lookup.dto;

/**
 * СКП - Статистический Классификатор Продукций
 * 
 * @author rustam
 *
 */
public class SKP implements HasCode {
	private String code;
	private String description;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
