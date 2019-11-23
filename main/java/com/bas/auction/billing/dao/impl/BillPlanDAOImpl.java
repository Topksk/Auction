package com.bas.auction.billing.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dao.BillPlanDAO;
import com.bas.auction.billing.dto.BillPlan;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BillPlanDAOImpl implements BillPlanDAO, GenericDAO<BillPlan> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public BillPlanDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<BillPlan> getEntityType() {
        return BillPlan.class;
    }

    @Override
    public String getSqlPath() {
        return "bill_plan";
    }

    @Override
    public BillPlan findCustomerBillPlan(Long customerId) {
        return daoutil.queryForObject(this, "get_customer_bill_plan", customerId);
    }

    @Override
    public BillPlan findSupplierBillPlan(Long supplierId) {
        return daoutil.queryForObject(this, "get_supplier_bill_plan", supplierId);
    }

    @Override
    @SpringTransactional
    public BillPlan insert(BillPlan billPlan, User user) {
        Object[] values = {billPlan.getPlanType(), user.getCustomerId(), user.getSupplierId(),
                billPlan.getEffectiveFrom(), billPlan.getEffectiveTo(), user.getUserId(),
                user.getUserId()};
        KeyHolder keyHolder = daoutil.insert(this, values);
        Long id = (Long) keyHolder.getKeys().get("bill_plan_id");
        billPlan.setBillPlanId(id);
        return billPlan;
    }
}