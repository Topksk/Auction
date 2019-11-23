package com.bas.auction.profile.address.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.address.dto.Address;

import java.util.List;

public interface AddressService {
	List<Address> findCustomerAddresses(Long customerId);

	List<Address> findSupplierAddresses(Long supplierId);

	Address create(User user, Address data);

	Address update(User user, Address data);

	boolean supplierLegalAddressExists(Long supplierId);

	boolean customerLegalAddressExists(Long customerId);
}
