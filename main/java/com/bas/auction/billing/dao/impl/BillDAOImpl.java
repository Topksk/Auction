package com.bas.auction.billing.dao.impl;

import com.bas.auction.billing.dao.BillDAO;
import com.bas.auction.billing.dto.Bill;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class BillDAOImpl implements BillDAO, GenericDAO<Bill> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public BillDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<Bill> getEntityType() {
        return Bill.class;
    }

    @Override
    public String getSqlPath() {
        return "bill";
    }

    @Override
    public List<Bill> findCustomerBills(Date from, Date to, Long customerId) {
        return daoutil.query(this, "get_customer_bills", customerId, from, to);
    }

    @Override
    public List<Bill> findSupplierBills(Date from, Date to, Long supplierId) {
        return daoutil.query(this, "get_supplier_bills", supplierId, from, to);
    }
}
