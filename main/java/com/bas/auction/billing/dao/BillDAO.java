package com.bas.auction.billing.dao;

import com.bas.auction.billing.dto.Bill;

import java.util.Date;
import java.util.List;

public interface BillDAO {
    List<Bill> findCustomerBills(Date from, Date to, Long customerId);

    List<Bill> findSupplierBills(Date from, Date to, Long supplierId);
}
