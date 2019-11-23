package com.bas.auction.profile.employee.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.profile.employee.dao.EmployeeDAO;
import com.bas.auction.profile.employee.dto.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EmployeeDAOImpl implements EmployeeDAO, GenericDAO<Employee> {
	private final DaoJdbcUtil daoutil;

	@Autowired
	public EmployeeDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
	}

	@Override
	public String getSqlPath() {
		return "employees";
	}

	@Override
	public Class<Employee> getEntityType() {
		return Employee.class;
	}

	@Override
	public Employee findByUserId(Long userId) {
		return daoutil.queryForObject(this, "get", userId);
	}

	@Override
	public List<Employee> findSupplierEmployees(Long supplierId) {
		return daoutil.query(this, "get_supplier_employee", supplierId);
	}

	@Override
	public List<Employee> findCustomerEmployees(Long customerId) {
		return daoutil.query(this, "get_customer_employee", customerId);
	}

	@Override
	public List<Map<String, Object>> findAllActiveBidAuthorsInfoForNotif(Long userId, Long negId) {
		return daoutil.queryForMapList(this, "all_active_bid_suppliers_emails", negId, userId);
	}

	@Override
	public List<Employee> findNegBidsSuppEmployees(Long negId) {
		return daoutil.query(this, "neg_bids_supp_emps", negId);
	}

	@Override
	public List<Employee> findTender2Stage1BidsSuppEmployees(Long negId) {
		return daoutil.query(this, "tender2_stage1_bid_supp_emps", negId);
	}

	@Override
	public Employee findSupplierMainEmployee(Long supplierId) {
		return daoutil.queryForObject(this, "get_supplier_main_user", supplierId);
	}

	@Override
	public Employee findBidAuthorEmployee(Long userId) {
		return daoutil.queryForObject(this, "get_bid_author_employee", userId);
	}
}
