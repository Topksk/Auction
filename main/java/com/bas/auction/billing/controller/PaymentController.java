package com.bas.auction.billing.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dto.Payment;
import com.bas.auction.billing.service.PaymentService;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/billing/payments", produces = APPLICATION_JSON_UTF8_VALUE)
public class PaymentController extends RestControllerExceptionHandler {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(MessageDAO messageDAO, PaymentService paymentService) {
        super(messageDAO);
        this.paymentService = paymentService;
    }

    @RequestMapping(path = "/customer")
    public List<Payment> findCustomerPayments(@RequestBody Map<String, Date> params,
                                              @CurrentUser User user) {
        MDC.put("action", "find cust payments");
        Date from = params.get("from");
        Date to = params.get("to");
        return paymentService.findCustomerPayments(from, to, user.getCustomerId());
    }

    @RequestMapping(path = "/supplier")
    public List<Payment> findSupplierPayments(@RequestBody Map<String, Date> params,
                                              @CurrentUser User user) {
        MDC.put("action", "find supp payments");
        Date from = params.get("from");
        Date to = params.get("to");
        return paymentService.findSupplierPayments(from, to, user.getSupplierId());
    }
}