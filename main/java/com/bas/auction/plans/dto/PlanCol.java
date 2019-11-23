package com.bas.auction.plans.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class PlanCol extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -491016956215571211L;
	@SerializedName("recid")
	private int colId;
	private long settingId;
	private String colName;
	private String colType;
	private String description;
	private Boolean isSystem;
	private Boolean required;
	private Boolean displayInForm;
	private Boolean displayInTemplate;
	@SerializedName("editable_col")
	private Boolean editable;
	private int orderNum;

	public int getColId() {
		return colId;
	}

	public void setColId(int colId) {
		this.colId = colId;
	}

	public long getSettingId() {
		return settingId;
	}

	public void setSettingId(long settingId) {
		this.settingId = settingId;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getDisplayInForm() {
		return displayInForm;
	}

	public void setDisplayInForm(Boolean displayInForm) {
		this.displayInForm = displayInForm;
	}

	public Boolean getDisplayInTemplate() {
		return displayInTemplate;
	}

	public void setDisplayInTemplate(Boolean displayInTemplate) {
		this.displayInTemplate = displayInTemplate;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public int getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
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

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getColType() {
		return colType;
	}

	public void setColType(String colType) {
		this.colType = colType;
	}
}
