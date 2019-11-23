package com.bas.auction.billing.dao.impl;

import com.bas.auction.billing.dao.BillDetailDAO;
import com.bas.auction.billing.dto.BillDetail;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BillDetailDAOImpl implements BillDetailDAO, GenericDAO<BillDetail> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public BillDetailDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<BillDetail> getEntityType() {
        return BillDetail.class;
    }

    @Override
    public String getSqlPath() {
        return "bill/detail";
    }

    @Override
    public List<BillDetail> findBillDetails(Long billId) {
        return daoutil.query(this, "get_bill_details", billId);
    }
}
