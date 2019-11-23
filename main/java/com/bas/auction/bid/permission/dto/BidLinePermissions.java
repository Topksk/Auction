package com.bas.auction.bid.permission.dto;

import java.io.Serializable;
import java.util.List;

public class BidLinePermissions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String supplierName;
	private Long supplierId;
	private long bidId;
	private int bidLineNum;
	private List<BidLinePermissionDetails> permissions;

	public List<BidLinePermissionDetails> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<BidLinePermissionDetails> permissions) {
		this.permissions = permissions;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public Long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Long supplierId) {
		this.supplierId = supplierId;
	}

	public long getBidId() {
		return bidId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
	}

	public int getBidLineNum() {
		return bidLineNum;
	}

	public void setBidLineNum(int bidLineNum) {
		this.bidLineNum = bidLineNum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (bidId ^ (bidId >>> 32));
		result = prime * result + bidLineNum;
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
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
		BidLinePermissions other = (BidLinePermissions) obj;
		if (bidId != other.bidId)
			return false;
		if (bidLineNum != other.bidLineNum)
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		return true;
	}
}