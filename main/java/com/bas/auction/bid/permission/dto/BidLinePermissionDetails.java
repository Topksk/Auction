package com.bas.auction.bid.permission.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class BidLinePermissionDetails extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1851719790742690204L;
	@SerializedName("recid")
	private Long requirementId;
	private Boolean permitted;
	private Boolean isSystem;
	private String rejectReason;
	private String description;

	public Long getRequirementId() {
		return requirementId;
	}

	public void setRequirementId(Long requirementId) {
		this.requirementId = requirementId;
	}

	public Boolean isPermitted() {
		return permitted;
	}

	public void setPermitted(Boolean permitted) {
		this.permitted = permitted;
	}

	public String getRejectReason() {
		return rejectReason;
	}

	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean isSystem() {
		return isSystem;
	}

	public void setSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((isSystem == null) ? 0 : isSystem.hashCode());
		result = prime * result + ((permitted == null) ? 0 : permitted.hashCode());
		result = prime * result + ((rejectReason == null) ? 0 : rejectReason.hashCode());
		result = prime * result + ((requirementId == null) ? 0 : requirementId.hashCode());
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
		BidLinePermissionDetails other = (BidLinePermissionDetails) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (isSystem == null) {
			if (other.isSystem != null)
				return false;
		} else if (!isSystem.equals(other.isSystem))
			return false;
		if (permitted == null) {
			if (other.permitted != null)
				return false;
		} else if (!permitted.equals(other.permitted))
			return false;
		if (rejectReason == null) {
			if (other.rejectReason != null)
				return false;
		} else if (!rejectReason.equals(other.rejectReason))
			return false;
		if (requirementId == null) {
			if (other.requirementId != null)
				return false;
		} else if (!requirementId.equals(other.requirementId))
			return false;
		return true;
	}
}