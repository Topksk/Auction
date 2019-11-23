package com.bas.auction.lookup.dto;

/**
 * КПВЭД - Классификатор Продукций по Видам Экономической Деятельности
 * 
 * @author rustam
 *
 */
public class KPVED implements HasCode {
	private String code;
	private String description;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
