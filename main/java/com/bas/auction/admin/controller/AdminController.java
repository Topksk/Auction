package com.bas.auction.admin.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.currency.dao.CurrencyDAO;
import com.bas.auction.currency.dto.Currency;
import com.bas.auction.currency.dto.ExchangeRate;
import com.bas.auction.currency.service.ExchangeRateLoaderService;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.docfiles.dao.DocFilePermissionDAO;
import com.bas.auction.docfiles.dto.DocFilePermission;
import com.bas.auction.neg.award.service.NegAwardService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.customer.dto.Customer;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.service.SupplierService;
import com.bas.auction.workday.dao.WorkdayDAO;
import com.bas.auction.workday.dto.Workday;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/admin", produces = APPLICATION_JSON_UTF8_VALUE)
public class AdminController extends RestControllerExceptionHandler {
    private final CustomerDAO customerDAO;
    private final SupplierDAO supplierDAO;
    private final SupplierService supplierService;
    private final AddressService addressService;
    private final BankAccountService bankAccountService;
    private final EmployeeService employeeService;
    private final CustomerSettingDAO customerSettingsDAO;
    private final DocFilePermissionDAO docFilePermissionDAO;
    private final NegotiationDAO negotiationDAO;
    private final NegAwardService negAwardService;
    private final CurrencyDAO currencyDAO;
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateLoaderService exchangeRateLoaderService;
    private final WorkdayDAO workdayDAO;

    @Autowired
    public AdminController(MessageDAO messageDAO, CustomerDAO customerDAO, SupplierDAO supplierDAO, SupplierService supplierService, AddressService addressService, BankAccountService bankAccountService, EmployeeService employeeService, CustomerSettingDAO customerSettingsDAO, DocFilePermissionDAO docFilePermissionDAO, NegotiationDAO negotiationDAO, NegAwardService negAwardService, CurrencyDAO currencyDAO, ExchangeRateService exchangeRateService, ExchangeRateLoaderService exchangeRateLoaderService, WorkdayDAO workdayDAO) {
        super(messageDAO);
        this.customerDAO = customerDAO;
        this.supplierDAO = supplierDAO;
        this.supplierService = supplierService;
        this.addressService = addressService;
        this.bankAccountService = bankAccountService;
        this.employeeService = employeeService;
        this.customerSettingsDAO = customerSettingsDAO;
        this.docFilePermissionDAO = docFilePermissionDAO;
        this.negotiationDAO = negotiationDAO;
        this.negAwardService = negAwardService;
        this.currencyDAO = currencyDAO;
        this.exchangeRateService = exchangeRateService;
        this.exchangeRateLoaderService = exchangeRateLoaderService;
        this.workdayDAO = workdayDAO;
    }

    @RequestMapping(path = "/supplierFullInfo/{supplierId}", method = GET)
    public Map<String, Object> findSupplierFullInfo(@PathVariable Long supplierId, @CurrentUser User user) {
        MDC.put("action", "find supp full info");
        Supplier supplier = supplierDAO.findById(user, supplierId);
        List<Address> addresses = addressService.findSupplierAddresses(supplierId);
        List<BankAccount> bankAccounts = bankAccountService.findSupplierBankAccounts(supplierId);
        List<Employee> employees = employeeService.findSupplierEmployees(supplierId);
        Map<String, Object> res = new HashMap<>();
        res.put("profile", supplier);
        res.put("addresses", addresses);
        res.put("bank_accounts", bankAccounts);
        res.put("employees", employees);
        return res;
    }

    @RequestMapping(path = "/customerProfile/{customerId}", method = GET)
    public Customer findCustomerProfile(@PathVariable Long customerId, @CurrentUser User user) {
        MDC.put("action", "find cust profile");
        return customerDAO.findByIdForUser(user, customerId);
    }

    @RequestMapping(path = "/customerAddress/{customerId}", method = GET)
    public List<Address> findCustomerAddress(@PathVariable Long customerId) {
        MDC.put("action", "find cust addresses");
        return addressService.findCustomerAddresses(customerId);
    }

    @RequestMapping(path = "/customerBanks/{customerId}", method = GET)
    public List<BankAccount> findCustomerBanks(@PathVariable Long customerId) {
        MDC.put("action", "find cust banks");
        return bankAccountService.findCustomerBankAccounts(customerId);
    }

    @RequestMapping(path = "/customerEmployees/{customerId}", method = GET)
    public List<Employee> findCustomerEmployees(@PathVariable Long customerId) {
        MDC.put("action", "find cust employees");
        return employeeService.findCustomerEmployees(customerId);
    }

    @RequestMapping(path = "/customerSettings/{customerId}", method = GET)
    public List<CustomerSetting> findCustomerSettings(@PathVariable Long customerId) {
        MDC.put("action", "find cust settings");
        return customerSettingsDAO.findCustomerSettings(customerId);
    }

    @RequestMapping(path = "/negs/{negId}", method = GET)
    public Negotiation findNeg(@PathVariable Long negId, @CurrentUser User user) {
        MDC.put("action", "find neg");
        return negotiationDAO.findAdminNeg(user, negId);
    }

    @RequestMapping(path = "/filePerms", method = GET)
    public List<DocFilePermission> findFilePermissions() {
        MDC.put("action", "find cust settings");
        return docFilePermissionDAO.findAll();
    }

    @RequestMapping(path = "/exchangeRates/{currency}", method = GET)
    public List<ExchangeRate> findExchangeRatest(@PathVariable String currency,
                                                 @RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to) throws ParseException {
        MDC.put("action", "find rates");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        Date fromDate = null;
        if (from != null)
            fromDate = sdf.parse(from);
        Date toDate = null;
        if (to != null)
            toDate = sdf.parse(to);
        return exchangeRateService.findExchangeRates(currency, fromDate, toDate);
    }

    @RequestMapping(path = "/currencies", method = GET)
    public List<Currency> findCurrencies() {
        MDC.put("action", "find rates");
        return currencyDAO.findAll();
    }

    @RequestMapping(path = "/workDays/{year}/{month}", method = GET)
    public List<Map<String, Object>> findWorkdays(@PathVariable int year,
                                                  @PathVariable int month) {
        MDC.put("action", "find work days");
        return workdayDAO.findWorkdays(year, month);
    }

    @RequestMapping(path = "/exchangeRates", params = "load", method = POST)
    public void loadExchangeRates() {
        MDC.put("action", "load rates");
        exchangeRateLoaderService.fetchAndSaveRates();
    }

    @RequestMapping(path = "/supplier/{supplierId}", params = "approve", method = POST)
    public Supplier approveSupplier(@PathVariable Long supplierId, @CurrentUser User user) {
        MDC.put("action", "approve supplier");
        return supplierService.approve(user, supplierId);
    }

    @RequestMapping(path = "/supplier/{supplierId}", params = "reject", method = POST)
    public Supplier rejectSupplier(@PathVariable Long supplierId, @CurrentUser User user) {
        MDC.put("action", "reject supplier");
        return supplierService.reject(user, supplierId);
    }

    @RequestMapping(path = "/filePerms", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void saveFilePermissions(@RequestBody List<DocFilePermission> permissions, @CurrentUser User user) {
        MDC.put("action", "save file perms");
        docFilePermissionDAO.update(user, permissions);
    }

    @RequestMapping(path = "/negs/{negId}", params = "manual_close", method = POST)
    public Negotiation manualClose(@PathVariable Long negId, @CurrentUser User user) {
        MDC.put("action", "manual close");
        return negAwardService.manualCloseNeg(user, negId);
    }

    @RequestMapping(path = "/currencies", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void saveCurrencies(@RequestBody List<Currency> currencies, @CurrentUser User user) {
        MDC.put("action", "save currencies");
        currencyDAO.update(user, currencies);
    }

    @RequestMapping(path = "/workDays", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Workday saveWorkday(@RequestBody Workday day, @CurrentUser User user) {
        MDC.put("action", "save workday");
        if (day.getDayId() > 0)
            workdayDAO.update(user, day);
        else
            workdayDAO.create(user, day);
        return day;
    }
}