package com.bas.auction.core.crypto;

import com.bas.auction.core.Conf;
import kz.gov.pki.kalkan.asn1.DERObjectIdentifier;
import kz.gov.pki.kalkan.jce.PrincipalUtil;
import kz.gov.pki.kalkan.jce.X509Principal;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.*;
import java.util.*;

@Component
public class CertUtil {
	private final static Logger logger = LoggerFactory.getLogger(CertUtil.class);
    private static final Map<String, String> oidMap = new HashMap<>();
    private final CertPathUtil certPathUtil;
	private final Conf conf;

	@Autowired
	public CertUtil(CertPathUtil certPathUtil, Conf conf) {
		this.certPathUtil = certPathUtil;
		this.conf = conf;
	}

	static {
		loadProvider();
		oidMap.put("2.5.4.3", "CN");
		oidMap.put("2.5.4.5", "SERIALNUMBER");
		oidMap.put("2.5.4.6", "C");
		oidMap.put("2.5.4.7", "L");
		oidMap.put("2.5.4.8", "ST");
		oidMap.put("2.5.4.10", "O");
		oidMap.put("2.5.4.11", "OU");
		oidMap.put("0.9.2342.19200300.100.1.25", "DC");
		oidMap.put("1.2.840.113549.1.9.1", "EMAILADDRESS");
		oidMap.put("2.5.4.4", "SURNAME");
		oidMap.put("2.5.4.42", "GIVENNAME");
	}

	public static boolean loadProvider() {
		try {
			if (Security.getProvider(KalkanProvider.PROVIDER_NAME) != null)
				return true;
			KalkanProvider provider = new KalkanProvider();
			Security.addProvider(provider);
			return true;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public Set<TrustAnchor> getTrustAnchors() {
		String cacerts = conf.getCACertsPath();
		File cadir = new File(cacerts);
		File[] files = cadir.listFiles();
		if (!cadir.exists() || !cadir.isDirectory() || files == null)
			return null;
		Set<TrustAnchor> ta = new HashSet<>();
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509", KalkanProvider.PROVIDER_NAME);
			for (File file : files) {
				if (!file.getName().matches("^.+\\.(cer|crt|pem)$"))
					continue;
				try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
					X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
					if (cert.getBasicConstraints() < 0)
						continue;
					ta.add(new TrustAnchor(cert, null));
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (NoSuchProviderException | CertificateException e) {
			logger.error(e.getMessage(), e);
		}
		return ta;
	}

	public boolean isNotResident(X509Certificate cert) {
		boolean notResident;
		PKIXCertPathValidatorResult result = certPathUtil.validatePath(cert, getTrustAnchors());
		notResident = isNotResident(result);
		return notResident;
	}

	public boolean isNotResident(PKIXCertPathValidatorResult result) {
		boolean notResident = false;
		String oid = conf.getNonresidentOID();
		if (result.getPolicyTree() == null)
			return false;
		Iterator<? extends PolicyNode> policy = result.getPolicyTree().getChildren();
		while (policy.hasNext()) {
            PolicyNode p = policy.next();
            notResident = p.getExpectedPolicies().contains(oid);
			if (notResident)
				break;
		}
		return notResident;
	}

	public Map<String, String> getCertSubjectTokens(X509Certificate cert) {
		try {
            Map<String, String> subjToken = new HashMap<>();
            X509Principal principal = PrincipalUtil.getSubjectX509Principal(cert);
			for (Object oid : principal.getOIDs()) {
				Vector<?> val = principal.getValues((DERObjectIdentifier) oid);
				if (val != null && val.size() > 0) {
					String o = oid.toString();
					String v = val.get(0).toString();
					if (oidMap.containsKey(o))
						o = oidMap.get(o);
					if ("CN".equalsIgnoreCase(o) || "GIVENNAME".equalsIgnoreCase(o))
						// remove quotes
						v = v.replaceAll("^\"", "").replaceAll("\"$", "").trim();
					subjToken.put(o, v.trim());
				}
			}
			return subjToken;
		} catch (CertificateEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

    public X509Certificate readCertificateFromFile(Path file) throws IOException, CertificateException {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(is);
        }
    }
}
