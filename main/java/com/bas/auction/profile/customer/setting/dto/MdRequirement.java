package com.bas.auction.profile.customer.setting.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class MdRequirement extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9098471678292831975L;
	@SerializedName("recid")
	private Long requirementId;
	private long settingId;
	private String negType;
	private String description;
	private String reqType;
	private Boolean isSystem;
	private Boolean applicableForStage1;
	private Boolean applicableForStage2;

	public Long getRequirementId() {
		return requirementId;
	}

	public void setRequirementId(Long requirementId) {
		this.requirementId = requirementId;
	}

	public long getSettingId() {
		return settingId;
	}

	public void setSettingId(long settingId) {
		this.settingId = settingId;
	}

	public String getNegType() {
		return negType;
	}

	public void setNegType(String negType) {
		this.negType = negType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsSystem() {
		return isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

	public void setApplicableForStage1(Boolean applicableForStage1) {
		this.applicableForStage1 = applicableForStage1;
	}

	public void setApplicableForStage2(Boolean applicableForStage2) {
		this.applicableForStage2 = applicableForStage2;
	}

	public Boolean isApplicableForStage2() {
		return applicableForStage2;
	}

	public Boolean isApplicableForStage1() {
		return applicableForStage1;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}
}
