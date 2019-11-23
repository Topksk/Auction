package com.bas.auction.profile.address.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.profile.address.dao.AddressDAO;
import com.bas.auction.profile.address.dto.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AddressDAOImpl implements AddressDAO, GenericDAO<Address> {

	private final static Logger logger = LoggerFactory.getLogger(AddressDAOImpl.class);
	private final DaoJdbcUtil daoutil;

	@Autowired
	public AddressDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
	}

	@Override
	public String getSqlPath() {
		return "addresses";
	}

	@Override
	public Class<Address> getEntityType() {
		return Address.class;
	}

	@Override
	public List<Address> findSupplierAddresses(Long supplierId) {
		return daoutil.query(this, "get_supplier", supplierId);
	}

	@Override
	public List<Address> findCustomerAddresses(Long customerId) {
		return daoutil.query(this, "get_customer", customerId);
	}

	@Override
	public List<Map<String, Object>> findCustomerAddressesForIntegra(Long customerId) {
		return daoutil.queryForMapList(this, "get_customer_for_integra", customerId);
	}

	@Override
	public List<Map<String, Object>> findSupplierAddressesForIntegra(Long supplierId) {
		return daoutil.queryForMapList(this, "get_supplier_for_integra", supplierId);
	}

	@Override
	public boolean customerLegalAddressExists(Long customerId) {
		logger.debug("check customer legal address: {}", customerId);
		return daoutil.exists(this, "check_customer_legal_address", customerId);
	}

	@Override
	public boolean supplierLegalAddressExists(Long supplierId) {
		logger.debug("check supplier legal address: {}", supplierId);
		return daoutil.exists(this, "check_supplier_legal_address", supplierId);
	}

	@Override
	public Address insert(User user, Address data) {
		Object[] values = { data.getAddressType().toString(), data.getCountry(), data.getKato(), data.getCity(),
				data.getAddressLine(), data.getPhoneNumber(), data.getSupplierId(), data.getCustomerId(),
				data.getEmail(), user.getUserId(), user.getUserId() };
		KeyHolder kh = daoutil.insert(this, values);
		data.setAddressId((Long) kh.getKeys().get("address_id"));
		return data;
	}

	@Override
	public Address update(User user, Address data) {
		Object[] values = { data.getAddressType().toString(), data.getCountry(), data.getKato(), data.getCity(),
				data.getAddressLine(), data.getPhoneNumber(), data.getSupplierId(), data.getCustomerId(),
				data.getEmail(), user.getUserId(), data.getAddressId() };
		daoutil.update(this, values);
		return data;
	}

}
