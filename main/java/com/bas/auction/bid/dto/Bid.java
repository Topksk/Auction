package com.bas.auction.bid.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Bid extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2617852076066180440L;

	@SerializedName("recid")
	private long bidId;
	private long negId;
	private long supplierId;
	private String bidStatus;
	private String bidComments;
	private Integer bidLimitDays;
	private Date publishDate;
	private Date cancelDate;
	private String cancelDesc;
	private String currencyCode;
	private BigDecimal sentExchangeRate;
	private BigDecimal unlockExchangeRate;
	private Long replacedBidId;
	private List<BidLine> bidLines;
	@ExcludeFromSearchIndex
	private Negotiation neg;
	private List<DocFile> bidFiles;
	@ExcludeFromSearchIndex
	private boolean hasBidReport;
	@ExcludeFromSearchIndex
	private Boolean hasBidPartReport;
	@ExcludeFromSearchIndex
	private Supplier supplier;
	@ExcludeFromSearchIndex
	private Address supplierLegalAddress;
	@ExcludeFromSearchIndex
	private Address supplierPhysicalAddress;
	@ExcludeFromSearchIndex
	private Employee author;

	public long getBidId() {
		return bidId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
	}

	public long getNegId() {
		return negId;
	}

	public void setNegId(long negId) {
		this.negId = negId;
	}

	public String getBidStatus() {
		return bidStatus;
	}

	public void setBidStatus(String bidStatus) {
		this.bidStatus = bidStatus;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public Date getCancelDate() {
		return cancelDate;
	}

	public void setCancelDate(Date cancelDate) {
		this.cancelDate = cancelDate;
	}

	public String getCancelDesc() {
		return cancelDesc;
	}

	public void setCancelDesc(String cancelDesc) {
		this.cancelDesc = cancelDesc;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getBidComments() {
		return bidComments;
	}

	public void setBidComments(String bidComments) {
		this.bidComments = bidComments;
	}

	public Integer getBidLimitDays() {
		return bidLimitDays;
	}

	public void setBidLimitDays(Integer bidLimitDay) {
		this.bidLimitDays = bidLimitDay;
	}

	public List<BidLine> getBidLines() {
		return bidLines;
	}

	public void setBidLines(List<BidLine> bidLines) {
		this.bidLines = bidLines;
	}

	public long getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(long supplierId) {
		this.supplierId = supplierId;
	}

	public Negotiation getNeg() {
		return neg;
	}

	public void setNeg(Negotiation neg) {
		this.neg = neg;
	}

	public List<DocFile> getBidFiles() {
		return bidFiles;
	}

	public void setBidFiles(List<DocFile> bidFiles) {
		this.bidFiles = bidFiles;
	}

	public boolean isHasBidReport() {
		return hasBidReport;
	}

	public void setHasBidReport(boolean hasBidReport) {
		this.hasBidReport = hasBidReport;
	}

	public Long getReplacedBidId() {
		return replacedBidId;
	}

	public void setReplacedBidId(Long replacedBidId) {
		this.replacedBidId = replacedBidId;
	}

	public BigDecimal getSentExchangeRate() {
		return sentExchangeRate;
	}

	public void setSentExchangeRate(BigDecimal exchangeRate) {
		this.sentExchangeRate = exchangeRate;
	}

	public BigDecimal getUnlockExchangeRate() {
		return unlockExchangeRate;
	}

	public void setUnlockExchangeRate(BigDecimal unlockExchangeRate) {
		this.unlockExchangeRate = unlockExchangeRate;
	}

	public Boolean getHasBidPartReport() {
		return hasBidPartReport;
	}

	public void setHasBidPartReport(Boolean hasBidPartReport) {
		this.hasBidPartReport = hasBidPartReport;
	}

	public Address getSupplierLegalAddress() {
		return supplierLegalAddress;
	}

	public void setSupplierLegalAddress(Address addr) {
		this.supplierLegalAddress = addr;
	}

	public Address getSupplierPhysicalAddress() {
		return supplierPhysicalAddress;
	}

	public void setSupplierPhysicalAddress(Address supplierPhysicalAddress) {
		this.supplierPhysicalAddress = supplierPhysicalAddress;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public Employee getAuthorEmployee() {
		return author;
	}

	public void setAuthorEmployee(Employee author) {
		this.author = author;
	}

	public boolean isReplacesOtherBid() {
		return getReplacedBidId() != null && getReplacedBidId() > 0;
	}

	public boolean isDraft() {
		return "DRAFT".equals(bidStatus);
	}
}
