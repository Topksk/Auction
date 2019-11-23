package com.bas.auction.profile.supplier.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.dto.SupplierSetting;
import com.bas.auction.profile.supplier.service.SupplierService;
import com.bas.auction.profile.supplier.setting.dao.SupplierSettingDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(path = "/supplierProfile", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierFullInfoController extends RestControllerExceptionHandler {
    private final SupplierDAO supplierDAO;
    private final AddressService addressService;
    private final BankAccountService bankAccountService;
    private final EmployeeService employeeService;
    private final SupplierSettingDAO supplierSettingDAO;

    @Autowired
    public SupplierFullInfoController(MessageDAO messageDAO, SupplierDAO supplierDAO, SupplierService supplierService,
                                      AddressService addressService, BankAccountService bankAccountService,
                                      EmployeeService employeeService, SupplierSettingDAO supplierSettingDAO) {
        super(messageDAO);
        this.supplierDAO = supplierDAO;
        this.addressService = addressService;
        this.bankAccountService = bankAccountService;
        this.employeeService = employeeService;
        this.supplierSettingDAO = supplierSettingDAO;
    }

    @RequestMapping(params = "full_info", method = GET)
    public Map<String, Object> findFullSupplierInfo(@CurrentUser User user) {
        MDC.put("action", "find full supp info");
        Supplier supplier = supplierDAO.findUserOrg(user);
        Employee employee = employeeService.findByUserId(user.getUserId());
        List<Address> addresses = addressService.findSupplierAddresses(user.getSupplierId());
        List<BankAccount> bankAccounts = bankAccountService.findSupplierBankAccounts(user.getSupplierId());
        List<Employee> employees = employeeService.findSupplierEmployees(user.getSupplierId());
        SupplierSetting supplierSetting = supplierSettingDAO.findMainSupplierSetting(user.getSupplierId());
        Map<String, Object> res = new HashMap<>();
        res.put("user_info", employee);
        res.put("profile", supplier);
        res.put("addresses", addresses);
        res.put("bank_accounts", bankAccounts);
        res.put("employees", employees);
        res.put("setting", supplierSetting);
        return res;
    }
}
