package com.bas.auction.core.crypto.gen;

import com.bas.auction.core.crypto.CertUtil;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

public abstract class KeyGenerator {
    public static final String DEFAULT_PASSWORD = "123456";
    public static final long VALIDITY_PERIOD = 3 * 365 * 24 * 60 * 60 * 1000L; // 3 years
    final Map<String, String> oidMap = new HashMap<>();

    public KeyGenerator() {
        CertUtil.loadProvider();
        oidMap.put("CN", "2.5.4.3");
        oidMap.put("SERIALNUMBER", "2.5.4.5");
        oidMap.put("C", "2.5.4.6");
        oidMap.put("L", "2.5.4.7");
        oidMap.put("ST", "2.5.4.8");
        oidMap.put("O", "2.5.4.10");
        oidMap.put("OU", "2.5.4.11");
        oidMap.put("DC", "0.9.2342.19200300.100.1.25");
        oidMap.put("EMAILADDRESS", "1.2.840.113549.1.9.1");
        oidMap.put("SURNAME", "2.5.4.4");
        oidMap.put("GIVENNAME", "2.5.4.42");
    }

    public void saveKeyStore(Path keyStorePath, char[] password, KeyStore keyStore) throws Exception {
        try (OutputStream outputStream = Files.newOutputStream(keyStorePath)) {
            keyStore.store(outputStream, password);
        }
    }

    public void extractAndSaveCertificateFromKeyStore(Path keyStorePath, char[] password, String alias, Path certificatePath) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12", KalkanProvider.PROVIDER_NAME);
        try (InputStream inputStream = Files.newInputStream(keyStorePath)) {
            keyStore.load(inputStream, password);
            Certificate certificate = keyStore.getCertificate(alias);
            Files.write(certificatePath, certificate.getEncoded());
        }
    }

    public KeyPair generateGostKeyPair() throws Exception {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("ECGOST34310", KalkanProvider.PROVIDER_NAME);

        kpGen.initialize(512, new SecureRandom());

        return kpGen.generateKeyPair();
    }

    public KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", KalkanProvider.PROVIDER_NAME);

        kpGen.initialize(2048, new SecureRandom());

        return kpGen.generateKeyPair();
    }
}
