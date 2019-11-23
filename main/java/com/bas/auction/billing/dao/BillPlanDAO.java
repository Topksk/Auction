package com.bas.auction.billing.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dto.BillPlan;

public interface BillPlanDAO {
    BillPlan findCustomerBillPlan(Long customerId);

    BillPlan findSupplierBillPlan(Long supplierId);

    BillPlan insert(BillPlan billPlan, User user);
}
