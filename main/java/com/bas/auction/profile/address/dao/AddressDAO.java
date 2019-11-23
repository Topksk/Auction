package com.bas.auction.profile.address.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.address.dto.Address;

import java.util.List;
import java.util.Map;

public interface AddressDAO {

	Address insert(User user, Address data);

	Address update(User user, Address data);

	boolean customerLegalAddressExists(Long customerId);

	boolean supplierLegalAddressExists(Long supplierId);

	List<Address> findCustomerAddresses(Long customerId);

	List<Address> findSupplierAddresses(Long supplierId);

	List<Map<String, Object>> findCustomerAddressesForIntegra(Long customerId);

	List<Map<String, Object>> findSupplierAddressesForIntegra(Long supplierId);
}
