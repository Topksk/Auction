package com.bas.auction.billing.service.impl;

import com.bas.auction.billing.dao.BillDAO;
import com.bas.auction.billing.dao.BillDetailDAO;
import com.bas.auction.billing.dao.BillsForPeriodDAO;
import com.bas.auction.billing.dto.Bill;
import com.bas.auction.billing.dto.BillDetail;
import com.bas.auction.billing.dto.BillsForPeriod;
import com.bas.auction.billing.service.BillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class BillServiceImpl implements BillService {
    private final Logger logger = LoggerFactory.getLogger(BillServiceImpl.class);
    private final BillsForPeriodDAO billsForPeriodDAO;
    private final BillDAO billDAO;
    private final BillDetailDAO billDetailDAO;

    @Autowired
    public BillServiceImpl(BillsForPeriodDAO billsForPeriodDAO, BillDAO billDAO, BillDetailDAO billDetailDAO) {
        this.billsForPeriodDAO = billsForPeriodDAO;
        this.billDAO = billDAO;
        this.billDetailDAO = billDetailDAO;
    }

    @Override
    public List<BillsForPeriod> findCustomerBillsForPeriod(Date from, Date to, Long customerId) {
        from = fromDate(from);
        to = toDate(to);
        logger.debug("from={}, to={}", from, to);
        List<BillsForPeriod> billsForPeriods = billsForPeriodDAO.findCustomerBillsForPeriod(from, to, customerId);
        billsForPeriods.forEach(billsForPeriod -> {
            List<Bill> customerBills = billDAO.findCustomerBills(billsForPeriod.getFromDate(), billsForPeriod.getToDate(), customerId);
            customerBills.forEach(this::setBillDetails);
            billsForPeriod.setBills(customerBills);
        });
        return billsForPeriods;
    }

    @Override
    public List<BillsForPeriod> findSupplierBillsForPeriod(Date from, Date to, Long supplierId) {
        from = fromDate(from);
        to = toDate(to);
        logger.debug("from={}, to={}", from, to);
        List<BillsForPeriod> billsForPeriods = billsForPeriodDAO.findSupplierBillsForPeriod(from, to, supplierId);
        billsForPeriods.forEach(billsForPeriod -> {
            List<Bill> supplierBills = billDAO.findSupplierBills(billsForPeriod.getFromDate(), billsForPeriod.getToDate(), supplierId);
            supplierBills.forEach(this::setBillDetails);
            billsForPeriod.setBills(supplierBills);
        });
        return billsForPeriods;
    }

    private void setBillDetails(Bill bill) {
        List<BillDetail> billDetails = billDetailDAO.findBillDetails(bill.getBidId());
        bill.setBillDetails(billDetails);
    }

    private Date fromDate(Date from) {
        if (from == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -1);
            return calendar.getTime();
        }
        return from;
    }

    private Date toDate(Date to) {
        if (to == null)
            return new Date();
        return to;
    }
}
