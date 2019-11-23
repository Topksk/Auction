package com.bas.auction.billing.service.impl;

import com.bas.auction.billing.dao.PaymentDAO;
import com.bas.auction.billing.dto.Payment;
import com.bas.auction.billing.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentDAO paymentDAO;

    @Autowired
    public PaymentServiceImpl(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    @Override
    public List<Payment> findCustomerPayments(Date from, Date to, Long customerId) {
        from = fromDate(from);
        to = toDate(to);
        logger.debug("from={}, to={}", from, to);
        return paymentDAO.findCustomerPayments(from, to, customerId);
    }

    @Override
    public List<Payment> findSupplierPayments(Date from, Date to, Long supplierId) {
        from = fromDate(from);
        to = toDate(to);
        logger.debug("from={}, to={}", from, to);
        return paymentDAO.findCustomerPayments(from, to, supplierId);
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
