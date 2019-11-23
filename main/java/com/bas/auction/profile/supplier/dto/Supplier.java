package com.bas.auction.profile.supplier.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.bas.auction.docfiles.dto.DocFile;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Supplier extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7386008379535635470L;
	@SerializedName("recid")
	private long supplierId;
	private Boolean nonresident;
	private Boolean legalEntity;
	private Boolean individual;
	private String identificationNumber;
	private String businessEntityType;
	private String businessEntityTypeCustom;
	private String rnn;
	private String nameRu;
	private String nameKz;
	private String stateRegNumber;
	private Date stateRegDate;
	private String stateRegDepartment;
	private String chiefFullName;
	private String chiefFullPosition;
	private String regStatus;
	private String country;
	@ExcludeFromSearchIndex
	private List<DocFile> regFiles;

	public long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(long supplierId) {
		this.supplierId = supplierId;
	}

	public Boolean isNonresident() {
		return nonresident != null && nonresident;
	}

	public void setNonresident(Boolean nonresident) {
		this.nonresident = nonresident;
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

	public Date getStateRegDate() {
		return stateRegDate;
	}

	public void setStateRegDate(Date stateRegDate) {
		this.stateRegDate = stateRegDate;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (supplierId ^ (supplierId >>> 32));
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
		Supplier other = (Supplier) obj;
		if (supplierId != other.supplierId)
			return false;
		return true;
	}

	public Boolean isLegalEntity() {
		return legalEntity != null && legalEntity;
	}

	public void setLegalEntity(Boolean legalEntity) {
		this.legalEntity = legalEntity;
	}

	public List<DocFile> getRegFiles() {
		return regFiles;
	}

	public void setRegFiles(List<DocFile> regFiles) {
		this.regFiles = regFiles;
	}

	public Boolean isIndividual() {
		return individual;
	}

	public void setIndividual(Boolean individual) {
		this.individual = individual;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getBusinessEntityTypeCustom() {
		return businessEntityTypeCustom;
	}

	public void setBusinessEntityTypeCustom(String businessEntityTypeCustom) {
		this.businessEntityTypeCustom = businessEntityTypeCustom;
	}
}