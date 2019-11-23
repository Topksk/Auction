package com.bas.auction.auth.dto;

import java.io.Serializable;
import java.util.Date;

public class OneTimeCode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 704828302276424420L;
	private Long userId;
	private String code;
	private Date activeFrom;
	private Date activeTo;
	private String email;



	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(Date activeFrom) {
		this.activeFrom = activeFrom;
	}

	public Date getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(Date activeTo) {
		this.activeTo = activeTo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
