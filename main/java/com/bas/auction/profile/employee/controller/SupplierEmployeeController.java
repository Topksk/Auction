package com.bas.auction.profile.employee.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/supplierProfile/employees", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierEmployeeController extends RestControllerExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(SupplierEmployeeController.class);
    private final EmployeeService employeeService;
    private final UserService userService;
    private final SupplierDAO supplierDAO;

    @Autowired
    public SupplierEmployeeController(MessageDAO messageDAO, EmployeeService employeeService, UserService userService, SupplierDAO supplierDAO) {
        super(messageDAO);
        this.employeeService = employeeService;
        this.userService = userService;
        this.supplierDAO = supplierDAO;
    }

    @RequestMapping(method = GET)
    public List<Employee> findEmployees(@CurrentUser User user) {
        MDC.put("action", "employees");
        return employeeService.findSupplierEmployees(user.getSupplierId());
    }

    @RequestMapping(path = "/current", method = GET)
    public Map<String, Object> findCurrentUserInfo(@CurrentUser User user) {
        MDC.put("action", "user info");
        Map<String, Object> res = new HashMap<>();
        /*boolean nonResident = supplierDAO.findIsNonresident(user.getSupplierId());
        boolean individual = supplierDAO.findIsIndividual(user.getSupplierId());*/
        Employee employee = employeeService.findByUserId(user.getUserId());
        res.put("user_info", employee);
        /*res.put("nonresident", nonResident);
        res.put("individual", individual);*/
        return res;
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Employee> create(@RequestBody Employee employee,
                                 @CurrentUser User user) {
        MDC.put("action", "create employee");
        employee.setCustomerId(null);
        if (!user.isSysadmin())
            employee.setSupplierId(user.getSupplierId());
        Long supplierId = employee.getSupplierId();
        employeeService.createSupplierEmployee(user, employee, supplierId);
        return employeeService.findSupplierEmployees(supplierId);
    }

    @RequestMapping(path = "/{employeeId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Employee> save(@PathVariable long employeeId,
                               @RequestBody Employee employee,
                               @CurrentUser User user) {
        MDC.put("action", "save employee");
        employee.setCustomerId(null);
        employee.setUserId(employeeId);
        if (!user.isSysadmin())
            employee.setSupplierId(user.getSupplierId());
        Long supplierId = employee.getSupplierId();
        employeeService.update(user, employee);
        return employeeService.findSupplierEmployees(supplierId);
    }

    @RequestMapping(path = "/current", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Employee savePersonalInfo(@RequestBody Employee employee,
                                     @CurrentUser User user) {
        MDC.put("action", "save personal info");
        employee.setCustomerId(null);
        if (!user.isSysadmin())
            employee.setSupplierId(user.getSupplierId());
        employeeService.updatePersonal(user, employee);
        return employeeService.findByUserId(user.getUserId());
    }

    @RequestMapping(path = "/current", params = "change_password", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Employee savePassword(@RequestBody Map<String, String> passwords,
                                 @CurrentUser User user) {
        MDC.put("action", "change password");
        String oldPassword = passwords.get("old_password");
        String newPassword = passwords.get("new_password");
        userService.changePassword(user.getUserId(), oldPassword, newPassword);
        return employeeService.findByUserId(user.getUserId());
    }
}