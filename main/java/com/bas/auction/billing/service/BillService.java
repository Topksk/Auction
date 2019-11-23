package com.bas.auction.billing.service;

import com.bas.auction.billing.dto.BillsForPeriod;

import java.util.Date;
import java.util.List;

public interface BillService {
    List<BillsForPeriod> findCustomerBillsForPeriod(Date from, Date to, Long customerId);

    List<BillsForPeriod> findSupplierBillsForPeriod(Date from, Date to, Long supplierId);
}
