package com.bas.auction.docfiles.dto;

import com.google.gson.annotations.SerializedName;

public class UserCertificate {

	@SerializedName("recid")
	private long certificateId;
	private long userId;
	private String serialNumber;

	public long getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(long certificateId) {
		this.certificateId = certificateId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
}
