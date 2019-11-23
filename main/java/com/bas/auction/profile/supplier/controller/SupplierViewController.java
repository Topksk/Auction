package com.bas.auction.profile.supplier.controller;

import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.service.SupplierService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(path = "/customer/suppliers", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierViewController extends RestControllerExceptionHandler {
    private final SupplierDAO supplierDAO;
    private final AddressService addressService;
    private final BankAccountService bankAccountService;

    @Autowired
    public SupplierViewController(MessageDAO messageDAO, SupplierDAO supplierDAO, SupplierService supplierService,
                                  AddressService addressService, BankAccountService bankAccountService) {
        super(messageDAO);
        this.supplierDAO = supplierDAO;
        this.addressService = addressService;
        this.bankAccountService = bankAccountService;
    }

    @RequestMapping(path = "/{supplierId}/organization", method = GET)
    public Supplier findUserOrg(@PathVariable long supplierId) {
        MDC.put("action", "find supp org for cust");

        return supplierDAO.findUserOrgByid(supplierId);
    }

    @RequestMapping(path = "/{supplierId}/banks", method = GET)
    public List<BankAccount> findBankAccounts(@PathVariable long supplierId) {
        MDC.put("action", "find supp banks for cust");

        return bankAccountService.findSupplierBankAccounts(supplierId);
    }

    @RequestMapping(path = "/{supplierId}/addresses", method = GET)
    public Map<String, Object> findAddresses(@PathVariable long supplierId) {
        MDC.put("action", "find supp addresses for cust");

        String country = supplierDAO.findSupplierCountry(supplierId);
        Map<String, Object> res = new HashMap<>();
        res.put("addresses", addressService.findSupplierAddresses(supplierId));
        res.put("country", country);
        return res;
    }
}
