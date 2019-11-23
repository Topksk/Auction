package com.bas.auction.billing.dto;


import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class BillDetail extends AuditableRow {
    @SerializedName("recid")
    private Long billDetailId;
    private Long billId;
    private Integer lineNum;
    private BigDecimal price;

    public Long getBillDetailId() {
        return billDetailId;
    }

    public void setBillDetailId(Long billDetailId) {
        this.billDetailId = billDetailId;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
