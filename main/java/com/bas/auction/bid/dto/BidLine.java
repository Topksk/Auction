package com.bas.auction.bid.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class BidLine extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8650731903934692533L;

	@ExcludeFromSearchIndex
	private long bidId;
	@SerializedName("recid")
	private int lineNum;
	private BigDecimal bidPrice;
	private Boolean participateTender2;
	private BigDecimal quantity;
	private BigDecimal discount;
	private BigDecimal totalPrice;
	private BigDecimal totalDiscountPrice;
	private Integer rank;
	private String bidLineStatus;
	private Boolean discountConfirmed;

	public long getBidId() {
		return bidId;
	}

	public void setBidId(long bidId) {
		this.bidId = bidId;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public BigDecimal getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(BigDecimal bidPrice) {
		this.bidPrice = bidPrice;
	}

	public Boolean getParticipateTender2() {
		return participateTender2;
	}

	public void setParticipateTender2(Boolean participateTender2) {
		this.participateTender2 = participateTender2;
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

	public BigDecimal getTotalDiscountPrice() {
		return totalDiscountPrice;
	}

	public void setTotalDiscountPrice(BigDecimal totalDiscountPrice) {
		this.totalDiscountPrice = totalDiscountPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (bidId ^ (bidId >>> 32));
		result = prime * result + lineNum;
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
		BidLine other = (BidLine) obj;
		if (bidId != other.bidId)
			return false;
		if (lineNum != other.lineNum)
			return false;
		return true;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getBidLineStatus() {
		return bidLineStatus;
	}

	public void setBidLineStatus(String bidLineStatus) {
		this.bidLineStatus = bidLineStatus;
	}

	public Boolean getDiscountConfirmed() {
		return discountConfirmed;
	}

	public void setDiscountConfirmed(Boolean discountConfirmed) {
		this.discountConfirmed = discountConfirmed;
	}
}
