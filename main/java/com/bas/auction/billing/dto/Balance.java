package com.bas.auction.billing.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class Balance extends AuditableRow {
    @SerializedName("recid")
    private Long balanceId;
    private Long customerId;
    private Long supplierId;
    private BigDecimal balance;

    public Long getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(Long balanceId) {
        this.balanceId = balanceId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
