package com.bas.auction.billing.dao.impl;

import com.bas.auction.billing.dao.PaymentDAO;
import com.bas.auction.billing.dto.Payment;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class PaymentDAOImpl implements PaymentDAO, GenericDAO<Payment> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public PaymentDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<Payment> getEntityType() {
        return Payment.class;
    }

    @Override
    public String getSqlPath() {
        return "payment";
    }

    @Override
    public List<Payment> findCustomerPayments(Date from, Date to, Long customerId) {
        return daoutil.query(this, "get_customer_payments", customerId, from, to);
    }

    @Override
    public List<Payment> findSupplierPayments(Date from, Date to, Long supplierId) {
        return daoutil.query(this, "get_supplier_payments", supplierId, from, to);
    }
}
