package com.bas.auction.auth.dto;

public class UserCertInfo {

	private String country;
	private String iin;
	private String firstName;
	private String lastName;
	private String middleName;
	private String orgName;
	private String bin;
	private String email;
	private boolean isLegalEntity;
	private boolean isIndividual;
	private boolean isNonResident;
	private boolean customerExists;
	private boolean supplierExists;
	private boolean supplierUserExists;
	private boolean customerUserExists;

	public String getIin() {
		return iin;
	}

	public void setIin(String iin) {
		this.iin = iin;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getBin() {
		if (bin == null)
			return iin;
		return bin;
	}

	public void setBin(String bin) {
		this.bin = bin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isLegalEntity() {
		return isLegalEntity;
	}

	public void setLegalEntity(boolean isLegalEntity) {
		this.isLegalEntity = isLegalEntity;
	}

	public boolean isIndividual() {
		return isIndividual;
	}

	public void setIndividual(boolean isIndividual) {
		this.isIndividual = isIndividual;
	}

	public boolean isNonResident() {
		return isNonResident;
	}

	public void setNonResident(boolean isNonResident) {
		this.isNonResident = isNonResident;
	}

	public boolean isSupplierExists() {
		return supplierExists;
	}

	public void setSupplierExists(boolean supplierExists) {
		this.supplierExists = supplierExists;
	}

	public boolean isCustomerExists() {
		return customerExists;
	}

	public void setCustomerExists(boolean customerExists) {
		this.customerExists = customerExists;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public boolean isSupplierUserExists() {
		return supplierUserExists;
	}

	public void setSupplierUserExists(boolean supplierUserExists) {
		this.supplierUserExists = supplierUserExists;
	}

	public boolean isCustomerUserExists() {
		return customerUserExists;
	}

	public void setCustomerUserExists(boolean customerUserExists) {
		this.customerUserExists = customerUserExists;
	}
}
