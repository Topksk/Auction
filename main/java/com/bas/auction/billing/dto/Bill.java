package com.bas.auction.billing.dto;


import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Bill extends AuditableRow {
    @SerializedName("recid")
    private Long bill;
    private Long negId;
    private Long bidId;
    private Date billDate;
    private BigDecimal price;
    private String description;
    private List<BillDetail> billDetails;

    public Long getBill() {
        return bill;
    }

    public void setBill(Long bill) {
        this.bill = bill;
    }

    public Long getNegId() {
        return negId;
    }

    public void setNegId(Long negId) {
        this.negId = negId;
    }

    public Long getBidId() {
        return bidId;
    }

    public void setBidId(Long bidId) {
        this.bidId = bidId;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BillDetail> getBillDetails() {
        return billDetails;
    }

    public void setBillDetails(List<BillDetail> billDetails) {
        this.billDetails = billDetails;
    }
}
