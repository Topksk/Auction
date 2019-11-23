package com.bas.auction.billing.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.dto.BillPlan;
import com.bas.auction.billing.service.BillPlanService;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(path = "/billing/billPlans", produces = APPLICATION_JSON_UTF8_VALUE)
public class BillPlanController extends RestControllerExceptionHandler {
    private final BillPlanService billPlanService;

    @Autowired
    public BillPlanController(MessageDAO messageDAO, BillPlanService billPlanService) {
        super(messageDAO);
        this.billPlanService = billPlanService;
    }

    @RequestMapping(path = "/customer")
    public BillPlan findCustomerBillPlan(@CurrentUser User user) {
        MDC.put("action", "find cust bill plan");
        return billPlanService.findCustomerBillPlan(user.getCustomerId());
    }

    @RequestMapping(path = "/supplier")
    public BillPlan findSupplierBillPlan(@CurrentUser User user) {
        MDC.put("action", "find supp bill plan");
        return billPlanService.findSupplierBillPlan(user.getSupplierId());
    }
}