package com.bas.auction.profile.address.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

public class Address extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9036738822725202747L;

	public enum AddressType {
		LEGAL, PHYSICAL_ADDRESS
	}

	@SerializedName("recid")
	private long addressId;
	private Long supplierId;
	private Long customerId;
	private AddressType addressType;
	private String kato;
	@ExcludeFromSearchIndex
	private String katoDesc;
	private String country;
	private String city;
	private String addressLine;
	private String phoneNumber;
	private String email;

	public long getAddressId() {
		return addressId;
	}

	public void setAddressId(long addressId) {
		this.addressId = addressId;
	}

	public String getKato() {
		return kato;
	}

	public void setKato(String kato) {
		this.kato = kato;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddressLine() {
		return addressLine;
	}

	public void setAddressLine(String addressLine) {
		this.addressLine = addressLine;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getKatoDesc() {
		return katoDesc;
	}

	public void setKatoDesc(String katoDesc) {
		this.katoDesc = katoDesc;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}

	public boolean isLegalAddress() {
		return addressType == AddressType.LEGAL;
	}

	public boolean isPhysicalAddress() {
		return addressType == AddressType.PHYSICAL_ADDRESS;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (addressId ^ (addressId >>> 32));
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
		Address other = (Address) obj;
		if (addressId != other.addressId)
			return false;
		return true;
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
}
