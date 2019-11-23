package com.bas.auction.auth.service.impl;

import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.UserCertInfoService;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.crypto.CertUtil;
import com.bas.auction.core.crypto.CertValidationError;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Map;

@Service
public class UserCertInfoServiceImpl implements UserCertInfoService {
	private final static Logger logger = LoggerFactory.getLogger(UserCertInfoServiceImpl.class);
	private final UserService userService;
	private final CustomerDAO customerDAO;
	private final SupplierDAO supplierDAO;
	private final CertUtil certUtil;

	@Autowired
	public UserCertInfoServiceImpl(CertUtil certUtil, UserService userDAO, CustomerDAO customerDAO,
			SupplierDAO supplierDAO) {
		this.certUtil = certUtil;
		this.userService = userDAO;
		this.customerDAO = customerDAO;
		this.supplierDAO = supplierDAO;
	}

	@Override
	public UserCertInfo findWithExistCheck(X509Certificate cert) throws CertValidationError {
		UserCertInfo info = getUserCertInfo(cert);
		String iin = info.getIin();
		String bin = info.getBin();
		info.setSupplierExists(supplierDAO.exists(bin));
		info.setCustomerExists(customerDAO.exists(bin));
		boolean customerUserExists = userService.customerUserExists(info.getIin(), info.getBin());
		info.setCustomerUserExists(customerUserExists);
		boolean supplierUserExists = userService.supplierUserExists(iin, bin);
		info.setSupplierUserExists(supplierUserExists);
		logger.debug("customer user exists: {}, supplier user exists: {}", info.isCustomerUserExists(),
				info.isSupplierUserExists());
		return info;
	}

	@Override
	public UserCertInfo getUserCertInfo(X509Certificate cert) {
		logger.debug("subjectDN: {}", cert.getSubjectX500Principal());
		Map<String, String> tokens = certUtil.getCertSubjectTokens(cert);
		UserCertInfo u = new UserCertInfo();
		u.setNonResident(certUtil.isNotResident(cert));
		u.setLegalEntity(u.isNonResident());
		u.setCountry(tokens.get("C"));
		if (u.isNonResident()) {
			if (tokens.containsKey("SERIALNUMBER"))
				u.setBin(u.getCountry() + tokens.get("SERIALNUMBER"));
			if (tokens.containsKey("EMAILADDRESS"))
				u.setIin(tokens.get("EMAILADDRESS"));
		} else if (tokens.containsKey("OU")) {
			u.setLegalEntity(true);
			u.setBin(tokens.get("OU").replace("BIN", ""));
			if (tokens.containsKey("SERIALNUMBER"))
				u.setIin(tokens.get("SERIALNUMBER").replace("IIN", ""));
		} else {
            u.setLegalEntity(false);
            u.setIin(tokens.get("SERIALNUMBER").replace("IIN", ""));
            u.setBin(u.getIin());
        }
		if (tokens.containsKey("CN")) {
			String[] cn = tokens.get("CN").split(" ");
			if (cn.length >= 2) {
				u.setLastName(cn[0]);
				u.setFirstName(cn[1]);
			}
		}
		if (tokens.containsKey("SURNAME"))
			u.setLastName(tokens.get("SURNAME"));
		if (tokens.containsKey("O"))
			u.setOrgName(tokens.get("O"));
		if (tokens.containsKey("GIVENNAME"))
			u.setMiddleName(tokens.get("GIVENNAME"));
		if (tokens.containsKey("EMAILADDRESS"))
			u.setEmail(tokens.get("EMAILADDRESS"));
		u.setIndividual(!u.isLegalEntity());
		return u;
	}
}
