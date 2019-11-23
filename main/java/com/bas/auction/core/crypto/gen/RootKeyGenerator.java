package com.bas.auction.core.crypto.gen;


import kz.gov.pki.kalkan.asn1.ASN1Sequence;
import kz.gov.pki.kalkan.asn1.DERIA5String;
import kz.gov.pki.kalkan.asn1.DERObjectIdentifier;
import kz.gov.pki.kalkan.asn1.DERSequence;
import kz.gov.pki.kalkan.asn1.x509.*;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import kz.gov.pki.kalkan.x509.X509V3CertificateGenerator;
import kz.gov.pki.kalkan.x509.extension.SubjectKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;

public class RootKeyGenerator extends KeyGenerator {
    private static final char[] DEFAULT_PASSWORD = KeyGenerator.DEFAULT_PASSWORD.toCharArray();
    public static final String ROOT_ALIAS = "root";
    public static final String ROOT_RSA_KEY_PATH = "tmp/keys/ca/rustam_root_rsa.p12";
    public static final String ROOT_GOST_KEY_PATH = "tmp/keys/ca/rustam_root_gost.p12";

    public Entry<X509Certificate, PrivateKey> readRsaRootKey() throws Exception {
        return readRootKey(Paths.get(ROOT_RSA_KEY_PATH), DEFAULT_PASSWORD);
    }

    public Entry<X509Certificate, PrivateKey> readGostRootKey() throws Exception {
        return readRootKey(Paths.get(ROOT_GOST_KEY_PATH), DEFAULT_PASSWORD);
    }

    public Entry<X509Certificate, PrivateKey> readRootKey(Path path, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12", KalkanProvider.PROVIDER_NAME);
        try (InputStream inputStream = Files.newInputStream(path)) {
            keyStore.load(inputStream, password);
            Enumeration<String> en = keyStore.aliases();
            while (en.hasMoreElements()) {
                String alias = en.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                if (certificate.getBasicConstraints() < 0)
                    continue;
                PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
                return new SimpleEntry<>(certificate, privateKey);
            }
        }
        return null;
    }

    public KeyStore generateGostPkcs12() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12", KalkanProvider.PROVIDER_NAME);
        keyStore.load(null, null);
        X500PrivateCredential rootCredential = createRootCredential(generateGostKeyPair());
        Certificate[] chain = {rootCredential.getCertificate()};
        keyStore.setKeyEntry(rootCredential.getAlias(), rootCredential.getPrivateKey(), null, chain);
        return keyStore;
    }

    public X500PrivateCredential createRootCredential(KeyPair rootPair) throws Exception {
        X509Certificate rootCert = generateRootCert(rootPair);
        return new X500PrivateCredential(rootCert, rootPair.getPrivate(), ROOT_ALIAS);
    }

    public X509Certificate generateRootCert(KeyPair pair) throws Exception {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(BigInteger.valueOf(1));
        String subjectDn = "CN=Rustam Gost CA";
        certGen.setIssuerDN(new X500Principal(subjectDn));
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal(subjectDn));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("1.2.398.3.10.1.1.1.2");
        addExtensions(certGen, pair.getPublic());
        return certGen.generate(pair.getPrivate(), KalkanProvider.PROVIDER_NAME);
    }

    private void addExtensions(X509V3CertificateGenerator certGen, PublicKey publicKey) throws CertificateParsingException {
        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(publicKey));
        certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
        certGen.addExtension(X509Extensions.CertificatePolicies, false, new DERSequence(createPolicyInfo()));
    }

    private PolicyInformation createPolicyInfo() {
        DERIA5String qualifier = new DERIA5String("http://root.gov.kz/cps");
        PolicyQualifierInfo pqi = new PolicyQualifierInfo(PolicyQualifierId.id_qt_cps, qualifier);
        ASN1Sequence sequence = new DERSequence(pqi);
        return new PolicyInformation(new DERObjectIdentifier("1.2.398.3.1.2"), sequence);
    }
}
