package com.bas.auction.profile.employee.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.employee.dto.Employee;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
	Employee findByUserId(Long userId);

	List<Employee> findSupplierEmployees(Long supplierId);

	List<Employee> findCustomerEmployees(Long customerId);

	List<Map<String, Object>> findAllActiveBidAuthorsInfoForNotif(Long userId, Long negId);

	List<Employee> findNegBidsSuppEmployees(Long negId);

	List<Employee> findTender2Stage1BidsSuppEmployees(Long negId);

	Employee findSupplierMainEmployee(Long supplierId);

	Employee findBidAuthorEmployee(Long userId);

	@SpringTransactional
	Employee registerSupplierMainUser(UserCertInfo certInfo, Employee emp);

	Employee update(User user, Employee data);

	Employee updatePersonal(User user, Employee data);

	User getEmployeeUser(Employee data);

	Employee createCustomerEmployee(User user, Employee data, Long customerId);

	Employee createSupplierEmployee(User user, Employee data, Long supplierId);

	void selfRegister(UserCertInfo certInfo, Employee emp);
}
