package com.bas.auction.profile.bankaccount.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/customerProfile/banks", produces = APPLICATION_JSON_UTF8_VALUE)
public class CustomerBankAccountsController extends RestControllerExceptionHandler {
    private final BankAccountService bankAccountService;

    @Autowired
    public CustomerBankAccountsController(MessageDAO messageDAO, BankAccountService bankAccountService) {
        super(messageDAO);
        this.bankAccountService = bankAccountService;
    }

    @RequestMapping(method = GET)
    public List<BankAccount> findBankAccounts(@CurrentUser User user) {
        MDC.put("action", "find banks");
        return bankAccountService.findCustomerBankAccounts(user.getCustomerId());
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<BankAccount> create(@RequestBody BankAccount bankAccount,
                                    @CurrentUser User user) {
        MDC.put("action", "create bank");
        bankAccount.setSupplierId(null);
        if (!user.isSysadmin())
            bankAccount.setCustomerId(user.getCustomerId());
        Long customerId = bankAccount.getCustomerId();
        bankAccountService.create(user, bankAccount);
        return bankAccountService.findCustomerBankAccounts(customerId);
    }

    @RequestMapping(path = "/{bankAccountId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<BankAccount> save(@PathVariable long bankAccountId,
                                  @RequestBody BankAccount bankAccount,
                                  @CurrentUser User user) {
        MDC.put("action", "save bank");
        bankAccount.setSupplierId(null);
        bankAccount.setAccountId(bankAccountId);
        if (!user.isSysadmin())
            bankAccount.setCustomerId(user.getCustomerId());
        Long customerId = bankAccount.getCustomerId();
        bankAccountService.update(user, bankAccount);
        return bankAccountService.findCustomerBankAccounts(customerId);
    }
}
