package com.bas.auction.profile.address.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.address.dao.AddressDAO;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.address.service.NoLegalAddressException;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
	private final static Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);
	private final Conf conf;
	private SupplierDAO supplierDAO;
	private final AddressDAO addressDAO;

	@Autowired
	public AddressServiceImpl(AddressDAO addressDAO, Conf conf) {
		this.addressDAO = addressDAO;
		this.conf = conf;
	}

	@Override
	public List<Address> findCustomerAddresses(Long customerId) {
		return addressDAO.findCustomerAddresses(customerId);
	}

	@Override
	public List<Address> findSupplierAddresses(Long supplierId) {
		return addressDAO.findSupplierAddresses(supplierId);
	}

	@Autowired
	public void setSupplierDAO(SupplierDAO supplierDAO) {
		this.supplierDAO = supplierDAO;
	}

	@Override
	public boolean customerLegalAddressExists(Long customerId) {
		return addressDAO.customerLegalAddressExists(customerId);
	}

	@Override
	public boolean supplierLegalAddressExists(Long supplierId) {
		return addressDAO.supplierLegalAddressExists(supplierId);
	}

	@Override
	@SpringTransactional
	public Address create(User user, Address data) {
		if (!user.isMainUserOrSysadmin())
			throw new AccessDeniedException();
		setCountry(data);
		return addressDAO.insert(user, data);
	}

	@Override
	@SpringTransactional
	public Address update(User user, Address data) {
		if (!user.isMainUserOrSysadmin())
			throw new AccessDeniedException();
		setCountry(data);
		data = addressDAO.update(user, data);
		validateLegalAddress(data);
		return data;
	}

	private void setCountry(Address data) {
		if (data.getSupplierId() != null && !supplierDAO.findIsNonresident(data.getSupplierId())) {
			data.setCountry(conf.getResidentCountry());
		}
	}

	private void validateLegalAddress(Address data) {
		Long orgId = data.getCustomerId();
		if (orgId != null && !customerLegalAddressExists(orgId)) {
			logger.error("legal customer address required: {}", orgId);
			throw new NoLegalAddressException();
		}
		orgId = data.getSupplierId();
		if (orgId != null && !supplierLegalAddressExists(orgId)) {
			logger.error("legal supplier address required: {}", orgId);
			throw new NoLegalAddressException();
		}
	}
}
