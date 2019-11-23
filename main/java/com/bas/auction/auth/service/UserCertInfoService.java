package com.bas.auction.auth.service;

import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.core.crypto.CertValidationError;

import java.security.cert.X509Certificate;

public interface UserCertInfoService {
	UserCertInfo findWithExistCheck(X509Certificate cert) throws CertValidationError;

	UserCertInfo getUserCertInfo(X509Certificate cert) throws CertValidationError;

}
