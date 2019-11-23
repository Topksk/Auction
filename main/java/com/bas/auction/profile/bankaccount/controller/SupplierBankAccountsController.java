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
@RequestMapping(path = "/supplierProfile/banks", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierBankAccountsController extends RestControllerExceptionHandler {
    private final BankAccountService bankAccountService;

    @Autowired
    public SupplierBankAccountsController(MessageDAO messageDAO, BankAccountService bankAccountService) {
        super(messageDAO);
        this.bankAccountService = bankAccountService;
    }

    @RequestMapping(method = GET)
    public List<BankAccount> findBankAccounts(@CurrentUser User user) {
        MDC.put("action", "find banks");
        return bankAccountService.findSupplierBankAccounts(user.getSupplierId());
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<BankAccount> create(@RequestBody BankAccount bankAccount,
                                    @CurrentUser User user) {
        MDC.put("action", "create bank");
        bankAccount.setCustomerId(null);
        if (!user.isSysadmin())
            bankAccount.setSupplierId(user.getSupplierId());
        Long supplierId = bankAccount.getSupplierId();
        bankAccountService.create(user, bankAccount);
        return bankAccountService.findSupplierBankAccounts(supplierId);
    }

    @RequestMapping(path = "/{bankAccountId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<BankAccount> save(@PathVariable long bankAccountId,
                                  @RequestBody BankAccount bankAccount,
                                  @CurrentUser User user) {
        MDC.put("action", "save bank");
        bankAccount.setCustomerId(null);
        bankAccount.setAccountId(bankAccountId);
        if (!user.isSysadmin())
            bankAccount.setSupplierId(user.getSupplierId());
        Long supplierId = bankAccount.getSupplierId();
        bankAccountService.update(user, bankAccount);
        return bankAccountService.findSupplierBankAccounts(supplierId);
    }
}
