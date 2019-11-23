package com.bas.auction.profile.customer.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.bas.auction.docfiles.dto.DocFile;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Customer extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1093064507524242350L;
	@SerializedName("recid")
	private long customerId;
	private boolean isOrganizer;
	private String identificationNumber;
	private String businessEntityType;
	private String rnn;
	private String nameRu;
	private String nameKz;
	private String stateRegNumber;
	private Date stateRegDate;
	private String stateRegDepartment;
	private String chiefFullName;
	private String chiefFullPosition;
	private String regStatus;
	private String headOrgIdentificationNumber;
	private String headOrgNameRu;
	private String headOrgNameKz;
	@ExcludeFromSearchIndex
	private String itemCodeListType;
	@ExcludeFromSearchIndex
	private List<DocFile> regFiles;

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getIdentificationNumber() {
		return identificationNumber;
	}

	public void setIdentificationNumber(String identificationNumber) {
		this.identificationNumber = identificationNumber;
	}

	public String getBusinessEntityType() {
		return businessEntityType;
	}

	public void setBusinessEntityType(String businessEntityType) {
		this.businessEntityType = businessEntityType;
	}

	public String getRnn() {
		return rnn;
	}

	public void setRnn(String rnn) {
		this.rnn = rnn;
	}

	public String getNameRu() {
		return nameRu;
	}

	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	public String getNameKz() {
		return nameKz;
	}

	public void setNameKz(String nameKz) {
		this.nameKz = nameKz;
	}

	public String getStateRegNumber() {
		return stateRegNumber;
	}

	public void setStateRegNumber(String stateRegNumber) {
		this.stateRegNumber = stateRegNumber;
	}

	public Date getStateRegDate() {
		return stateRegDate;
	}

	public void setStateRegDate(Date stateRegDate) {
		this.stateRegDate = stateRegDate;
	}

	public String getStateRegDepartment() {
		return stateRegDepartment;
	}

	public void setStateRegDepartment(String stateRegDepartment) {
		this.stateRegDepartment = stateRegDepartment;
	}

	public String getChiefFullName() {
		return chiefFullName;
	}

	public void setChiefFullName(String chiefFullName) {
		this.chiefFullName = chiefFullName;
	}

	public String getChiefFullPosition() {
		return chiefFullPosition;
	}

	public void setChiefFullPosition(String chiefFullPosition) {
		this.chiefFullPosition = chiefFullPosition;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public String getHeadOrgIdentificationNumber() {
		return headOrgIdentificationNumber;
	}

	public void setHeadOrgIdentificationNumber(String headOrgIdentificationNumber) {
		this.headOrgIdentificationNumber = headOrgIdentificationNumber;
	}

	public String getHeadOrgNameKz() {
		return headOrgNameKz;
	}

	public void setHeadOrgNameKz(String headOrgNameKz) {
		this.headOrgNameKz = headOrgNameKz;
	}

	public boolean getIsOrganizer() {
		return isOrganizer;
	}

	public void setIsOrganizer(boolean isOrginizer) {
		this.isOrganizer = isOrginizer;
	}

	public String getItemCodeListType() {
		return itemCodeListType;
	}

	public void setItemCodeListType(String itemCodeListType) {
		this.itemCodeListType = itemCodeListType;
	}

	public String getHeadOrgNameRu() {
		return headOrgNameRu;
	}

	public void setHeadOrgNameRu(String headOrgNameRu) {
		this.headOrgNameRu = headOrgNameRu;
	}

	public List<DocFile> getRegFiles() {
		return regFiles;
	}

	public void setRegFiles(List<DocFile> regFiles) {
		this.regFiles = regFiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (customerId ^ (customerId >>> 32));
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
		Customer other = (Customer) obj;
		if (customerId != other.customerId)
			return false;
		return true;
	}
}
