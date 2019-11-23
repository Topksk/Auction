package com.bas.auction.billing.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dto.BillPlan;

import java.util.Date;

public interface BillPlanService {
    BillPlan findCustomerBillPlan(Long customerId);

    BillPlan findSupplierBillPlan(Long supplierId);

    BillPlan createCustomerBillPlan(String planType, Date from, Date to, User user);

    BillPlan createSupplierBillPlan(String planType, Date from, Date to, User user);
}
