package com.bas.auction.docfiles.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

public class DocFilePermission extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7593808202421410605L;

	@SerializedName("recid")
	private Long permissionId;
	private String attribute;
	private String value;
	private Boolean customerRead;
	private Boolean customerSign;
	private Boolean customerRemove;
	private Boolean supplierRead;
	private Boolean supplierSign;
	private Boolean supplierRemove;

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean isCustomerRead() {
		return customerRead != null && customerRead;
	}

	public void setCustomerRead(Boolean customerRead) {
		this.customerRead = customerRead;
	}

	public Boolean isCustomerSign() {
		return customerSign != null && customerSign;
	}

	public void setCustomerSign(Boolean customerSign) {
		this.customerSign = customerSign;
	}

	public Boolean isCustomerRemove() {
		return customerRemove != null && customerRemove;
	}

	public void setCustomerRemove(Boolean customerRemove) {
		this.customerRemove = customerRemove;
	}

	public Boolean isSupplierRead() {
		return supplierRead != null && supplierRead;
	}

	public void setSupplierRead(Boolean supplierRead) {
		this.supplierRead = supplierRead;
	}

	public Boolean isSupplierSign() {
		return supplierSign != null && supplierSign;
	}

	public void setSupplierSign(Boolean supplierSign) {
		this.supplierSign = supplierSign;
	}

	public Boolean isSupplierRemove() {
		return supplierRemove != null && supplierRemove;
	}

	public void setSupplierRemove(Boolean supplierRemove) {
		this.supplierRemove = supplierRemove;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("attribute: ").append(attribute).append(", value: ").append(value);
		sb.append(", customer: [ read: ").append(customerRead).append(", remove: ").append(customerRemove)
				.append(", sign: ").append(customerSign).append("]");
		sb.append("supplier: [ read: ").append(supplierRead).append(", remove: ").append(supplierRemove)
				.append(", sign: ").append(supplierSign).append("]");
		return sb.toString();
	}
}
