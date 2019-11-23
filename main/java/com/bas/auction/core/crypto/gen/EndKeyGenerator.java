package com.bas.auction.core.crypto.gen;


import kz.gov.pki.kalkan.asn1.*;
import kz.gov.pki.kalkan.asn1.x509.*;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import kz.gov.pki.kalkan.x509.X509V3CertificateGenerator;
import kz.gov.pki.kalkan.x509.extension.AuthorityKeyIdentifierStructure;
import kz.gov.pki.kalkan.x509.extension.SubjectKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map.Entry;

public class EndKeyGenerator extends KeyGenerator {
    private static final char[] DEFAULT_PASSWORD = KeyGenerator.DEFAULT_PASSWORD.toCharArray();
    private String endEntityAlias = "end";
    //String subjectDn = "SERIALNUMBER=IIN840317000029, CN=Agzamov Rustam, OU=BIN081140000436, O=AO Samruk-Kazyna, L=Astana, S=AST, C=KZ";

    public synchronized void generateAndSavePkcs12(String keyStoreName, String subjectDn, boolean nonResident) throws Exception {
        endEntityAlias = keyStoreName;
        Path rsaKeyStorePath = Paths.get("static/keys/rsa_" + keyStoreName + ".p12");
        Path rsaCertPath = Paths.get("static/keys/rsa_" + keyStoreName + ".cer");
        RootKeyGenerator rootKeyGenerator = new RootKeyGenerator();
        Entry<X509Certificate, PrivateKey> rootCredential = rootKeyGenerator.readRsaRootKey();
        KeyPair rsaKeyPair = generateRsaKeyPair();
        KeyStore rsaKeyStore = generatePkcs12(rsaKeyPair, subjectDn, nonResident, rootCredential);
        saveKeyStore(rsaKeyStorePath, DEFAULT_PASSWORD, rsaKeyStore);
        extractAndSaveCertificateFromKeyStore(rsaKeyStorePath, DEFAULT_PASSWORD, keyStoreName, rsaCertPath);

        rootCredential = rootKeyGenerator.readGostRootKey();
        Path gostKeyStorePath = Paths.get("static/keys/gost_" + keyStoreName + ".p12");
        Path gostCertPath = Paths.get("static/keys/gost_" + keyStoreName + ".cer");
        KeyPair gostKeyPair = generateGostKeyPair();
        KeyStore gostKeyStore = generatePkcs12(gostKeyPair, subjectDn, nonResident, rootCredential);

        saveKeyStore(gostKeyStorePath, DEFAULT_PASSWORD, gostKeyStore);
        extractAndSaveCertificateFromKeyStore(gostKeyStorePath, DEFAULT_PASSWORD, keyStoreName, gostCertPath);
    }

    private KeyStore generatePkcs12(KeyPair endKeyPair, String subjectDn, boolean nonResident, Entry<X509Certificate, PrivateKey> rootCredential) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12", KalkanProvider.PROVIDER_NAME);
        keyStore.load(null, null);
        X500PrivateCredential endCredential;
        if (nonResident)
            endCredential = createNonResidentEndEntityCredential(endKeyPair, subjectDn, rootCredential.getValue(), rootCredential.getKey());
        else
            endCredential = createResidentEndEntityCredential(endKeyPair, subjectDn, rootCredential.getValue(), rootCredential.getKey());
        Certificate[] chain = {endCredential.getCertificate(), rootCredential.getKey()};
        keyStore.setCertificateEntry(RootKeyGenerator.ROOT_ALIAS, rootCredential.getKey());
        keyStore.setKeyEntry(endCredential.getAlias(), endCredential.getPrivateKey(), null, chain);
        return keyStore;
    }

    private X500PrivateCredential createResidentEndEntityCredential(KeyPair endPair, String subjectDn, PrivateKey caKey, X509Certificate caCert)
            throws Exception {
        X509Certificate endCert = generateResidentEndEntityCert(endPair.getPublic(), subjectDn, caKey, caCert);
        return new X500PrivateCredential(endCert, endPair.getPrivate(), endEntityAlias);
    }

    private X500PrivateCredential createNonResidentEndEntityCredential(KeyPair endPair, String subjectDn, PrivateKey caKey, X509Certificate caCert)
            throws Exception {
        X509Certificate endCert = generateNonResidentEndEntityCert(endPair.getPublic(), subjectDn, caKey, caCert);
        return new X500PrivateCredential(endCert, endPair.getPrivate(), endEntityAlias);
    }

    private X509Certificate generateResidentEndEntityCert(PublicKey publicKey, String subjectDn, PrivateKey caKey, X509Certificate caCert) throws Exception {
        X509V3CertificateGenerator certGen = createEndEntiryCertGenerator(publicKey, subjectDn, caCert);
        return certGen.generate(caKey, KalkanProvider.PROVIDER_NAME);
    }

    private X509Certificate generateNonResidentEndEntityCert(PublicKey publicKey, String subjectDn, PrivateKey caKey, X509Certificate caCert) throws Exception {
        X509V3CertificateGenerator certGen = createEndEntiryCertGenerator(publicKey, subjectDn, caCert);
        certGen.addExtension(X509Extensions.CertificatePolicies, false, new DERSequence(createNonResidentPolicyInfo()));
        return certGen.generate(caKey, KalkanProvider.PROVIDER_NAME);
    }


    private X509V3CertificateGenerator createEndEntiryCertGenerator(PublicKey publicKey, String subjectDn, X509Certificate caCert) throws CertificateParsingException {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        SecureRandom random = new SecureRandom();
        certGen.setSerialNumber(new BigInteger(128, random));
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal(subjectDn, oidMap));
        certGen.setPublicKey(publicKey);
        if (publicKey.getAlgorithm().equals("RSA"))
            certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        else
            certGen.setSignatureAlgorithm("1.2.398.3.10.1.1.1.2"); //GOST3411withECGOST3410
        addExtensions(certGen, publicKey, caCert);
        return certGen;
    }

    private void addExtensions(X509V3CertificateGenerator certGen, PublicKey publicKey, X509Certificate caCert) throws CertificateParsingException {
        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(publicKey));
        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        certGen.addExtension(X509Extensions.KeyUsage, true, getKeyUsage());
    }

    private KeyUsage getKeyUsage() {
        return new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.nonRepudiation);
    }

    private PolicyInformation createNonResidentPolicyInfo() {
        DERIA5String qualifier = new DERIA5String("https://ca.gamma.kz/application/pdf/rules.pdf");
        PolicyQualifierInfo pqi1 = new PolicyQualifierInfo(PolicyQualifierId.id_qt_cps, qualifier);

        ASN1EncodableVector av = new ASN1EncodableVector();
        av.add(new DERInteger(1));
        NoticeReference noticeReference = new NoticeReference(DisplayText.CONTENT_TYPE_IA5STRING,
                "Gamma Technologies Research Laboratory LLP", new DERSequence(av));
        UserNotice userNotice = new UserNotice(noticeReference, "Participant of e-procurement");
        PolicyQualifierInfo pqi2 = new PolicyQualifierInfo(PolicyQualifierId.id_qt_unotice, userNotice);

        ASN1Sequence sequence = new DERSequence(new PolicyQualifierInfo[]{pqi1, pqi2});
        return new PolicyInformation(new DERObjectIdentifier("1.3.6.1.4.1.6801.3.4"), sequence);
    }
}
