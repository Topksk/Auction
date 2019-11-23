package com.bas.auction.core.crypto;

import com.bas.auction.core.crypto.CertValidationError.Type;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Component
public class CertPathUtil {
	private final static Logger logger = LoggerFactory.getLogger(CertPathUtil.class);

	public PKIXCertPathValidatorResult validatePath(X509Certificate cert, Set<TrustAnchor> trustAnchors)
			throws CertValidationError {

		try {
			// CertStore store = CertStore.getInstance("Collection", params);
			CertificateFactory fact = CertificateFactory.getInstance("X.509", KalkanProvider.PROVIDER_NAME);

			List<X509Certificate> certChain = new ArrayList<>();

			certChain.add(cert);
			CertPath certPath = fact.generateCertPath(certChain);
			CertPathValidator validator = CertPathValidator.getInstance("PKIX", KalkanProvider.PROVIDER_NAME);
			PKIXParameters param = new PKIXParameters(trustAnchors);

			// param.addCertStore(store);
			param.setRevocationEnabled(false);
			param.setSigProvider(KalkanProvider.PROVIDER_NAME);

			// устанавливаем проверку на период действия сертификата,
			// чтобы для сертификатов с истекшим периодом проходили проверку,
			// так как такая проверка осуществляется в другом месте
			param.setDate(cert.getNotAfter());

			X509CertSelector selector = new X509CertSelector();

			selector.setKeyUsage(cert.getKeyUsage());
			param.setTargetCertConstraints(selector);

			return (PKIXCertPathValidatorResult) validator.validate(certPath, param);
		} catch (CertPathValidatorException e) {
			throw new CertValidationError(Type.CERT_PATH_VALIDATION_EXCEPTION);
		} catch (InvalidAlgorithmParameterException e) {
			if (e.getMessage().matches(".*trustAnchors.*non-empty"))
				throw new CertValidationError(Type.TRUSTED_ANCHOR_LIST_IS_EMPTY);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public List<? extends Certificate> getCertPathCAList(X500Principal principal, Set<TrustAnchor> ta)
			throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
			CertPathBuilderException {
		List<X509Certificate> certs = ta.stream().map(TrustAnchor::getTrustedCert).collect(toList());
		CertStoreParameters param = new CollectionCertStoreParameters(certs);
		CertStore store = CertStore.getInstance("Collection", param, KalkanProvider.PROVIDER_NAME);
		CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", KalkanProvider.PROVIDER_NAME);
		X509CertSelector selector = new X509CertSelector();
		selector.setSubject(principal);
		PKIXParameters buildParams = new PKIXBuilderParameters(ta, selector);
		buildParams.addCertStore(store);
		buildParams.setRevocationEnabled(false);
		buildParams.setSigProvider(KalkanProvider.PROVIDER_NAME);
		CertPathBuilderResult result = builder.build(buildParams);
		CertPath certPath = result.getCertPath();
		return certPath.getCertificates();
	}

}
