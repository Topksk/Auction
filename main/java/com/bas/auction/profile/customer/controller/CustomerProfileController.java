package com.bas.auction.profile.customer.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.customer.dto.Customer;
import com.bas.auction.profile.customer.service.CustomerService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/customerProfile/organizations", produces = APPLICATION_JSON_UTF8_VALUE)
public class CustomerProfileController extends RestControllerExceptionHandler {
    private final CustomerDAO customerDAO;
    private final CustomerService customerService;

    @Autowired
    public CustomerProfileController(MessageDAO messageDAO, CustomerDAO customerDAO, CustomerService customerService) {
        super(messageDAO);
        this.customerDAO = customerDAO;
        this.customerService = customerService;
    }

    @RequestMapping(path = "/current", method = GET)
    public Customer findUserOrg(@CurrentUser User user) {
        MDC.put("action", "find current org");
        return customerDAO.findUserOrg(user);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Customer create(@RequestBody Customer customer,
                           @CurrentUser User user) {
        MDC.put("action", "create customer");
        if (!user.isSysadmin())
            throw new AccessDeniedException();
        return customerService.create(user, customer);
    }

    @RequestMapping(path = "/{customerId}", params = "save", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Customer save(@PathVariable long customerId,
                         @RequestBody Customer customer,
                         @CurrentUser User user) {
        MDC.put("action", "save customer");
        if (!user.isSysadmin())
            customerId = user.getCustomerId();
        customer.setCustomerId(customerId);
        return customerDAO.update(user, customer);
    }

    @RequestMapping(path = "/{customerId}", params = "confirm_registration", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Customer confirmRegistration(@PathVariable long customerId,
                                        @RequestBody Customer customer,
                                        @CurrentUser User user) {
        MDC.put("action", "confirm customer reg");
        if (!user.isSysadmin())
            customerId = user.getCustomerId();
        customer.setCustomerId(customerId);
        customerDAO.update(user, customer);
        return customerService.confirmRegistration(user.getUserId(), customerId);
    }

    @RequestMapping(path = "/{customerId}", params = "approve", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Customer confirmApprove(@PathVariable long customerId,
                                   @RequestBody Customer customer,
                                   @CurrentUser User user) throws IOException {
        MDC.put("action", "approve customer");
        if (!user.isSysadmin())
            throw new AccessDeniedException();
        customer.setCustomerId(customerId);
        customerDAO.update(user, customer);
        return customerService.approveCustomer(user.getUserId(), customerId);
    }
}
