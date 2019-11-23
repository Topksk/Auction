package com.bas.auction.docfiles.dao;

import com.bas.auction.core.crypto.CertValidationError;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public interface UserCertificateDAO {
	Long findCert(Long userId, X509Certificate cert) throws CertValidationError, IOException, CertificateException;

	Long findUserId(Long certId);

	Long create(Long userId, X509Certificate cert) throws IOException, CertificateException;
}
