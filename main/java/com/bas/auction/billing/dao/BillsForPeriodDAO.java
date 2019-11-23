package com.bas.auction.billing.dao;

import com.bas.auction.billing.dto.BillsForPeriod;

import java.util.Date;
import java.util.List;

public interface BillsForPeriodDAO {
    List<BillsForPeriod> findCustomerBillsForPeriod(Date from, Date to, Long customerId);

    List<BillsForPeriod> findSupplierBillsForPeriod(Date from, Date to, Long supplierId);
}
