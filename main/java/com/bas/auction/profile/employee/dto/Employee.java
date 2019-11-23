package com.bas.auction.profile.employee.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Employee extends Person {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1516518284181384806L;
	@SerializedName("recid")
	private long userId;
	private Boolean active;
	private Boolean mainUser;
	private Long supplierId;
	private Long customerId;
	private String login;
	private String password;
	private String email;
	private String phoneNumber;
	private Long userSessionId;
	private Date endActiveDate;
	private String userPosition;
	private String ksk_pos;
	private String com_pos;



	public String getCom_pos() {
		return com_pos;
	}

	public void setCom_pos(String com_pos) {
		this.com_pos = com_pos;
	}


	public String getKsk_pos() {
		return ksk_pos;
	}

	public void setKsk_pos(String ksk_pos) {
		this.ksk_pos = ksk_pos;
	}

	public Long getUserSessionId() {
		return userSessionId;
	}

	public void setUserSessionId(Long userSessionId) {
		this.userSessionId = userSessionId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Boolean isMainUser() {
		return mainUser != null && mainUser;
	}

	public void setMainUser(Boolean mainUser) {
		this.mainUser = mainUser;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Date getEndActiveDate() {
		return endActiveDate;
	}

	public void setEndActiveDate(Date endActiveDate) {
		this.endActiveDate = endActiveDate;
	}

	public String getUserPosition() {
		return userPosition;
	}

	public void setUserPosition(String userPosition) {
		this.userPosition = userPosition;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userId ^ (userId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (userId != other.userId)
			return false;
		return true;
	}
}
