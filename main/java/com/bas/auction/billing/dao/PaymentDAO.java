package com.bas.auction.billing.dao;

import com.bas.auction.billing.dto.Payment;

import java.util.Date;
import java.util.List;


public interface PaymentDAO {
    List<Payment> findCustomerPayments(Date from, Date to, Long customerId);

    List<Payment> findSupplierPayments(Date from, Date to, Long supplierId);
}
