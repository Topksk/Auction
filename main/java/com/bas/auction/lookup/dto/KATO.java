package com.bas.auction.lookup.dto;

/**
 * КАТО - Классификатор Административно-Территориальных Образований
 * 
 * @author rustam
 *
 */
public class KATO implements HasCode {
	private String code;
	private String region;
	private String county;
	private String district;
	private String town;

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

}