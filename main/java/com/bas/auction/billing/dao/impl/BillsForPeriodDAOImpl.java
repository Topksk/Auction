package com.bas.auction.billing.dao.impl;

import com.bas.auction.billing.dao.BillsForPeriodDAO;
import com.bas.auction.billing.dto.BillsForPeriod;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class BillsForPeriodDAOImpl implements BillsForPeriodDAO, GenericDAO<BillsForPeriod> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public BillsForPeriodDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "bill";
    }

    @Override
    public Class<BillsForPeriod> getEntityType() {
        return BillsForPeriod.class;
    }

    @Override
    public List<BillsForPeriod> findCustomerBillsForPeriod(Date from, Date to, Long customerId) {
        return daoutil.query(this, "get_customer_bills_for_period", customerId, from, to);
    }

    @Override
    public List<BillsForPeriod> findSupplierBillsForPeriod(Date from, Date to, Long supplierId) {
        return daoutil.query(this, "get_supplier_bills_for_period", supplierId, from, to);
    }
}
