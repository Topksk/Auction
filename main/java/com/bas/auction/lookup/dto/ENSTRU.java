package com.bas.auction.lookup.dto;

/**
 * ЕНСТРУ - Единый Нормативный Справочник Товаров, Работ и Услуг
 * 
 * @author rustam
 *
 */
public class ENSTRU implements HasCode {

	private String code;
	private String shortDesc;
	private String fullDesc;
	private String type;
	private String uom;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getFullDesc() {
		return fullDesc;
	}

	public void setFullDesc(String fullDesc) {
		this.fullDesc = fullDesc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

}
