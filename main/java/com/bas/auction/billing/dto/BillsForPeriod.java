package com.bas.auction.billing.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class BillsForPeriod {
    @SerializedName("recid")
    private Long billsForPeriodId;
    private Long billNumber;
    private Long customerId;
    private Long supplierId;
    private Date fromDate;
    private Date toDate;
    private BigDecimal periodPrice;
    private String description;
    private List<Bill> bills;

    public Long getBillsForPeriodId() {
        return billsForPeriodId;
    }

    public void setBillsForPeriodId(Long billsForPeriodId) {
        this.billsForPeriodId = billsForPeriodId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date date) {
        this.fromDate = date;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public BigDecimal getPeriodPrice() {
        return periodPrice;
    }

    public void setPeriodPrice(BigDecimal periodPrice) {
        this.periodPrice = periodPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public Long getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(Long billNumber) {
        this.billNumber = billNumber;
    }
}
