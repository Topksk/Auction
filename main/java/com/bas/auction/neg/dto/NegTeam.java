package com.bas.auction.neg.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

public class NegTeam extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7010195378340394482L;
	@ExcludeFromSearchIndex
	private long negId;
	@SerializedName("recid")
	private long userId;
	private String roleCode;
	private String fullName;
	private String memberPosition;

	public long getNegId() {
		return negId;
	}

	public void setNegId(long negId) {
		this.negId = negId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMemberPosition() {
		return memberPosition;
	}

	public void setMemberPosition(String position) {
		this.memberPosition = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (negId ^ (negId >>> 32));
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
		NegTeam other = (NegTeam) obj;
		if (negId != other.negId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}

}
