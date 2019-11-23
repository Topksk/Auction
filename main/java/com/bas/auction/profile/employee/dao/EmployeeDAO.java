package com.bas.auction.profile.employee.dao;

import com.bas.auction.profile.employee.dto.Employee;

import java.util.List;
import java.util.Map;

public interface EmployeeDAO {
	Employee findByUserId(Long userId);

	List<Employee> findSupplierEmployees(Long supplierId);

	List<Employee> findCustomerEmployees(Long customerId);

	List<Map<String, Object>> findAllActiveBidAuthorsInfoForNotif(Long userId, Long negId);

	List<Employee> findNegBidsSuppEmployees(Long negId);

	List<Employee> findTender2Stage1BidsSuppEmployees(Long negId);

	Employee findSupplierMainEmployee(Long supplierId);

	Employee findBidAuthorEmployee(Long userId);
}
