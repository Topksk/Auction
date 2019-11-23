package com.bas.auction.profile.employee.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.employee.dao.EmployeeDAO;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.dto.Person;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.employee.service.IndividualsCanRegisterOnlyOneEmployeeException;
import com.bas.auction.profile.employee.service.PersonService;
import com.bas.auction.profile.employee.service.UserWithGivenIinAlreadyExistsException;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private final EmployeeDAO employeeDAO;
    private final PersonService personService;
    private final UserService userService;
    private final CryptoUtils cryptoUtils;
    private CustomerDAO customerDAO;
    private SupplierDAO supplierDAO;

    @Autowired
    public EmployeeServiceImpl(EmployeeDAO employeeDAO, CryptoUtils cryptoUtils, PersonService personService,
                               UserService userService) {
        this.employeeDAO = employeeDAO;
        this.cryptoUtils = cryptoUtils;
        this.personService = personService;
        this.userService = userService;
    }

    @Autowired
    public void setCustomerDAO(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    @Autowired
    public void setSupplierDAO(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    @Override
    public Employee findByUserId(Long userId) {
        logger.debug("find employee: {}", userId);
        Employee emp = employeeDAO.findByUserId(userId);
        if (emp == null)
            logger.warn("employee not found: {}", userId);
        return emp;
    }

    @Override
    public List<Employee> findSupplierEmployees(Long supplierId) {
        logger.debug("find supplier employees: {}", supplierId);
        return employeeDAO.findSupplierEmployees(supplierId);
    }

    @Override
    public List<Employee> findCustomerEmployees(Long customerId) {
        logger.debug("find customer employees: {}", customerId);
        return employeeDAO.findCustomerEmployees(customerId);
    }

    @Override
    public List<Map<String, Object>> findAllActiveBidAuthorsInfoForNotif(Long userId, Long negId) {
        return employeeDAO.findAllActiveBidAuthorsInfoForNotif(userId, negId);
    }

    @Override
    public List<Employee> findNegBidsSuppEmployees(Long negId) {
        logger.debug("find neg bids supplier employees: {}", negId);
        List<Employee> emp = employeeDAO.findNegBidsSuppEmployees(negId);
        if (emp.isEmpty()) {
            logger.info("no neg bids suppliers employees found: {}", negId);
        }
        return emp;
    }

    @Override
    public List<Employee> findTender2Stage1BidsSuppEmployees(Long negId) {
        logger.debug("get tender2 stage1 bids supp employees: negId={}", negId);
        List<Employee> emp = employeeDAO.findTender2Stage1BidsSuppEmployees(negId);
        if (emp.isEmpty()) {
            logger.error("no tender2 stage1 bids suppliers employees found: negId={}", negId);
        }
        return emp;
    }

    @Override
    public Employee findSupplierMainEmployee(Long supplierId) {
        logger.debug("get supplier main employee: {}", supplierId);
        Employee emp = employeeDAO.findSupplierMainEmployee(supplierId);
        if (emp == null)
            logger.warn("supplier main employee not found: {}", supplierId);
        return emp;
    }

    @Override
    public Employee findBidAuthorEmployee(Long userId) {
        logger.debug("get bid author employee: {}", userId);
        Employee emp = employeeDAO.findBidAuthorEmployee(userId);
        if (emp == null)
            logger.warn("bid author employee not found: {}", userId);
        return emp;
    }

    @Override
    public User getEmployeeUser(Employee data) {
        User u = new User();
        u.setUserId(data.getUserId());
        u.setActive(data.isActive());
        u.setEmail(data.getEmail());
        u.setUserPosition(data.getUserPosition());
        u.setPhoneNumber(data.getPhoneNumber());
        u.setMainUser(data.isMainUser());
        u.setPassword(data.getPassword());
        u.setPersonId(data.getPersonId());
        u.setLogin(data.getLogin());
        u.setCustomerId(data.getCustomerId());
        u.setSupplierId(data.getSupplierId());
        return u;
    }

    @Override
    @SpringTransactional
    public Employee createCustomerEmployee(User user, Employee data, Long customerId) {
        String bin = customerDAO.findBin(customerId);
        String login = userService.getCustomerLogin(data.getIin(), bin);
        if (userService.findIsUserLoginExists(login))
            throw new UserWithGivenIinAlreadyExistsException();
        data.setLogin(login);
        data.setSupplierId(null);
        data.setCustomerId(customerId);
        data.setNonresident(false);
        data = create(user, data);
        return data;
    }

    @Override
    @SpringTransactional
    public Employee createSupplierEmployee(User user, Employee data, Long supplierId) {
        validateIndividual(supplierId);
        String bin = supplierDAO.findBin(supplierId);
        boolean nonresident = supplierDAO.findIsNonresident(supplierId);
        String login = userService.newSupplierUserLogin(data.getIin(), bin, nonresident);
        if (userService.findIsUserLoginExists(login))
            throw new UserWithGivenIinAlreadyExistsException();
        data.setLogin(login);
        data.setSupplierId(supplierId);
        data.setCustomerId(null);
        data.setNonresident(nonresident);
        data = create(user, data);
        return data;
    }

    private void validateIndividual(Long supplierId) {
        boolean isIndividual = supplierDAO.findIsIndividual(supplierId);
        if (!isIndividual)
            return;
        List<Employee> supplierEmployees = findSupplierEmployees(supplierId);
        if (!supplierEmployees.isEmpty())
            throw new IndividualsCanRegisterOnlyOneEmployeeException();
    }

    private Employee create(User user, Employee data) {
        createOrUpdatePerson(user, data);
        User u = getEmployeeUser(data);
        String password = u.getPassword();
        boolean genPasswd = password == null;
        if (genPasswd) {
            password = cryptoUtils.generateRandomString();
            u.setPassword(password);
        }
        userService.create(user, u);
        data.setUserId(u.getUserId());
        if (genPasswd)
            userService.sendUserRegNotif(data.getEmail(), password);
        return data;
    }

    private void createOrUpdatePerson(User user, Person data) {
        Long id = personService.findIdByIin(data.getIin());
        if (id != null) {
            data.setPersonId(id);
            personService.update(user, data);
        } else {
            personService.create(user, data);
        }
    }

    @Override
    @SpringTransactional
    public void selfRegister(UserCertInfo certInfo, Employee emp) {
        setEmployeeIinFromCertInfo(certInfo, emp);
        User sysadmin = User.sysadmin();
        Long supplierId = supplierDAO.findIdByBin(certInfo.getBin());
        if (supplierId != null) {
            createSupplierEmployee(sysadmin, emp, supplierId);
        }
        Long customerId = customerDAO.findIdByBin(certInfo.getBin());
        if (customerId != null) {
            createCustomerEmployee(sysadmin, emp, customerId);
        }
        userService.sendEmailActivationMail(emp.getIin(), certInfo.getBin());
        if (!emp.isMainUser())
            userService.sendNewUserNotifToMainUser(certInfo.getBin());
    }

    @Override
    @SpringTransactional
    public Employee registerSupplierMainUser(UserCertInfo certInfo, Employee emp) {
        setEmployeeIinFromCertInfo(certInfo, emp);
        User sysadmin = User.sysadmin();
        Long supplierId = supplierDAO.findIdByBin(certInfo.getBin());
        if (supplierId != null) {
            emp = createSupplierEmployee(sysadmin, emp, supplierId);
            userService.sendEmailActivationMail(emp.getIin(), certInfo.getBin());
            return emp;
        }
        return null;
    }

    private void setEmployeeIinFromCertInfo(UserCertInfo certInfo, Employee emp) {
        if (certInfo.isNonResident()) {
            emp.setIin(certInfo.getEmail());
        } else {
            emp.setIin(certInfo.getIin());
        }
    }

    @Override
    @SpringTransactional
    public Employee update(User user, Employee data) {
        personService.update(user, data);
        User u = getEmployeeUser(data);
        userService.update(user, u);
        return data;
    }

    @Override
    @SpringTransactional
    public Employee updatePersonal(User user, Employee data) {
        personService.update(user, data);
        User u = getEmployeeUser(data);
        userService.updatePersonal(user, u);
        return data;
    }
}
