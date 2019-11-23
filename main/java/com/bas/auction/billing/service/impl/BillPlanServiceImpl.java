package com.bas.auction.billing.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dao.BillPlanDAO;
import com.bas.auction.billing.dto.BillPlan;
import com.bas.auction.billing.service.BillPlanService;
import com.bas.auction.core.spring.SpringTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class BillPlanServiceImpl implements BillPlanService {
    private final BillPlanDAO billPlanDAO;

    @Autowired
    public BillPlanServiceImpl(BillPlanDAO billPlanDAO) {
        this.billPlanDAO = billPlanDAO;
    }

    @Override
    public BillPlan findCustomerBillPlan(Long customerId) {
        return billPlanDAO.findCustomerBillPlan(customerId);
    }

    @Override
    public BillPlan findSupplierBillPlan(Long supplierId) {
        return billPlanDAO.findSupplierBillPlan(supplierId);
    }

    @Override
    @SpringTransactional
    public BillPlan createCustomerBillPlan(String planType, Date from, Date to, User user) {
        from = fromDate(from);
        to = toDate(to);
        BillPlan billPlan = new BillPlan();
        billPlan.setCustomerId(user.getCustomerId());
        billPlan.setPlanType(planType);
        billPlan.setEffectiveFrom(from);
        billPlan.setEffectiveTo(to);
        return billPlanDAO.insert(billPlan, user);
    }

    @Override
    @SpringTransactional
    public BillPlan createSupplierBillPlan(String planType, Date from, Date to, User user) {
        from = fromDate(from);
        to = toDate(to);
        BillPlan billPlan = new BillPlan();
        billPlan.setSupplierId(user.getSupplierId());
        billPlan.setPlanType(planType);
        billPlan.setEffectiveFrom(from);
        billPlan.setEffectiveTo(to);
        return billPlanDAO.insert(billPlan, user);
    }

    private Date fromDate(Date from) {
        if (from == null)
            from = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(from);
        calendar.set(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private Date toDate(Date to) {
        if (to == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(to);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        return calendar.getTime();
    }
}
