package com.bas.auction.profile.employee.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/customerProfile/employees", produces = APPLICATION_JSON_UTF8_VALUE)
public class CustomerEmployeeController extends RestControllerExceptionHandler {
    private final EmployeeService employeeService;
    private final UserService userService;

    @Autowired
    public CustomerEmployeeController(MessageDAO messageDAO, EmployeeService employeeService, UserService userService) {
        super(messageDAO);
        this.employeeService = employeeService;
        this.userService = userService;
    }

    @RequestMapping(method = GET)
    public List<Employee> findEmployees(@CurrentUser User user) {
        MDC.put("action", "employees");
        return employeeService.findCustomerEmployees(user.getCustomerId());
    }

    @RequestMapping(path = "/current", method = GET)
    public Employee findCurrentUserInfo(@CurrentUser User user) {
        MDC.put("action", "user info");
        return employeeService.findByUserId(user.getUserId());
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Employee> create(@RequestBody Employee employee,
                                 @CurrentUser User user) {
        MDC.put("action", "create employee");
        employee.setSupplierId(null);
        if (!user.isSysadmin())
            employee.setCustomerId(user.getCustomerId());
        long customerId = employee.getCustomerId();
        employeeService.createCustomerEmployee(user, employee, customerId);
        return employeeService.findCustomerEmployees(customerId);
    }

    @RequestMapping(path = "/{employeeId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Employee> save(@PathVariable long employeeId,
                               @RequestBody Employee employee,
                               @CurrentUser User user) {
        MDC.put("action", "save employee");
        employee.setSupplierId(null);
        employee.setUserId(employeeId);
        if (!user.isSysadmin())
            employee.setCustomerId(user.getCustomerId());
        long customerId = employee.getCustomerId();
        employeeService.update(user, employee);
        return employeeService.findCustomerEmployees(customerId);
    }

    @RequestMapping(path = "/current", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Employee savePersonalInfo(@RequestBody Employee employee,
                                     @CurrentUser User user) {
        MDC.put("action", "save personal info");
        employee.setSupplierId(null);
        if (!user.isSysadmin())
            employee.setCustomerId(user.getCustomerId());
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