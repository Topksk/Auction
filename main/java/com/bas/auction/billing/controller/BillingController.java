package com.bas.auction.billing.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dto.BillsForPeriod;
import com.bas.auction.billing.service.BillService;
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
@RequestMapping(path = "/billing/bills", produces = APPLICATION_JSON_UTF8_VALUE)
public class BillingController extends RestControllerExceptionHandler {
    private final BillService billService;

    @Autowired
    public BillingController(MessageDAO messageDAO, BillService billService) {
        super(messageDAO);
        this.billService = billService;
    }

    @RequestMapping(path = "/customer")
    public List<BillsForPeriod> findCustomerBillsForPeriod(@RequestBody Map<String, Date> params,
                                                           @CurrentUser User user) {
        MDC.put("action", "find cust bills");
        Date from = params.get("from");
        Date to = params.get("to");
        return billService.findCustomerBillsForPeriod(from, to, user.getCustomerId());
    }

    @RequestMapping(path = "/supplier")
    public List<BillsForPeriod> findSupplierBillsForPeriod(@RequestBody Map<String, Date> params,
                                                           @CurrentUser User user) {
        MDC.put("action", "find supp bills");
        Date from = params.get("from");
        Date to = params.get("to");
        return billService.findSupplierBillsForPeriod(from, to, user.getSupplierId());
    }
}