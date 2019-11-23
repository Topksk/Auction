package com.bas.auction.plans.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class PlanFile extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2018545535341463020L;

	@SerializedName("recid")
	private long planFileId;
	private long customerId;
	private long fileId;
	private String fileName;
	private String fileSize;
	private Long parseLogFileId;
	private String status;

	public long getPlanFileId() {
		return planFileId;
	}

	public void setPlanFileId(long planFileId) {
		this.planFileId = planFileId;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public long getFileId() {
		return fileId;
	}

	public void setFileId(long fileId) {
		this.fileId = fileId;
	}

	public Long getParseLogFileId() {
		return parseLogFileId;
	}

	public void setParseLogFileId(Long parseLogFileId) {
		this.parseLogFileId = parseLogFileId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

}
