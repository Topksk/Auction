package com.bas.auction.core.crypto;

import com.bas.auction.core.Conf;
import kz.gov.pki.kalkan.asn1.*;
import kz.gov.pki.kalkan.asn1.x509.*;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class CRLUtil {
    private final static Logger logger = LoggerFactory.getLogger(CRLUtil.class);
    private final static Object lock = new Object();
    private static volatile Collection<X509CRL> crlset;
    private final CertPathUtil certPathUtil;
    private final Conf conf;

    @Autowired
    public CRLUtil(CertPathUtil certPathUtil, Conf conf) {
        this.certPathUtil = certPathUtil;
        this.conf = conf;
    }

    public boolean isRevoked(X509Certificate cert, Set<TrustAnchor> ta) {
        try {
            logger.trace("check revocation for cert: {}", cert.getSubjectX500Principal().toString());
            initCrlSet(ta);
            Iterator<? extends CRL> crls = getCRLs(cert, ta).iterator();
            if (crls.hasNext())
                logger.trace("CRL for cert verification found");
            else
                logger.warn("CRL for cert verification not found: {}", cert.getSubjectX500Principal());
            while (crls.hasNext()) {
                CRL crl = crls.next();
                boolean revoked = crl.isRevoked(cert);
                if (revoked) {
                    logger.warn("revoked: {}", cert.getSubjectX500Principal().toString());
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.trace("cert is not revoked: {}", cert.getSubjectX500Principal().toString());
        return false;
    }

    private Collection<? extends CRL> getCRLs(X509Certificate cert, Set<TrustAnchor> ta)
            throws IOException, URISyntaxException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException, CertStoreException, CertPathBuilderException {
        CertStoreParameters ccsp = new CollectionCertStoreParameters(crlset);
        CertStore cs = CertStore.getInstance("Collection", ccsp, KalkanProvider.PROVIDER_NAME);
        CRLSelector selector = getCRLSelector(cert, ta);
        Iterator<? extends CRL> crls = cs.getCRLs(selector).iterator();
        if (!crls.hasNext()) {
            downloadFromCRLDP(cert, ta);
            downloadFromDeltaCRLDP(cert, ta);
        } else {
            boolean downloadBaseCRL = true;
            boolean downloadDeltaCRL = !getDeltaCRLDistributionPoints(cert).isEmpty();
            while (crls.hasNext()) {
                X509CRL crl = (X509CRL) crls.next();
                boolean isDelta = getDeltaCrlNumber(crl) != null;
                if (isDelta)
                    downloadDeltaCRL = false;
                else
                    downloadBaseCRL = false;
                if (!downloadBaseCRL && !downloadDeltaCRL)
                    break;
            }
            if (downloadBaseCRL)
                downloadFromCRLDP(cert, ta);
            if (downloadDeltaCRL)
                downloadFromDeltaCRLDP(cert, ta);
        }
        return cs.getCRLs(selector);
    }

    private CRLSelector getCRLSelector(X509Certificate cert, Set<TrustAnchor> ta)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
            CertPathBuilderException {
        X509CRLSelector selector = new X509CRLSelector();
        selector.setCertificateChecking(cert);
        certPathUtil.getCertPathCAList(cert.getIssuerX500Principal(), ta).stream()
                .map(c -> (X509Certificate) c)
                .map(X509Certificate::getSubjectX500Principal)
                .forEach(selector::addIssuer);
        return selector;
    }

    private void downloadFromCRLDP(X509Certificate cert, Set<TrustAnchor> ta) throws IOException, URISyntaxException {
        List<URI> urls = getCRLDistributionPoints(cert);
        downloadCRLs(urls, ta);
    }

    private void downloadFromDeltaCRLDP(X509Certificate cert, Set<TrustAnchor> ta)
            throws URISyntaxException, IOException {
        List<URI> urls = getDeltaCRLDistributionPoints(cert);
        downloadCRLs(urls, ta);
    }

    private void initCrlSet(Set<TrustAnchor> ta)
            throws CertificateException, NoSuchProviderException, CRLException, IOException {
        if (crlset == null || crlset.isEmpty()) {
            crlset = readCRLFiles(ta);
            return;
        }
        Date now = new Date();
        for (X509CRL crl : crlset) {
            if (crl.getThisUpdate().compareTo(now) > 0 || crl.getNextUpdate().compareTo(now) < 0) {
                crlset = readCRLFiles(ta);
                return;
            }
        }
    }

    public Path getCRLLoc() {
        String crlLoc = conf.getCrlPath();
        return Paths.get(crlLoc);
    }

    private Collection<X509CRL> readCRLFiles(Set<TrustAnchor> ta)
            throws IOException, CertificateException, NoSuchProviderException, CRLException {
        synchronized (lock) {
            Collection<X509CRL> set = new CopyOnWriteArraySet<>();
            Path dir = getCRLLoc();
            if (!Files.isDirectory(dir) || !Files.exists(dir)) {
                logger.warn("Directory for CRL not found!");
                return set;
            }
            DirectoryStream<Path> files = Files.newDirectoryStream(dir);
            CertificateFactory cf = CertificateFactory.getInstance("X.509", KalkanProvider.PROVIDER_NAME);
            for (Path file : files) {
                if (!file.toString().endsWith(".crl"))
                    continue;
                X509CRL crl = null;
                try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
                    crl = (X509CRL) cf.generateCRL(is);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    crl = verifyRefreshCRL(file, crl, ta);
                    if (crl != null)
                        set.add(crl);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return set;
        }
    }

    private X509CRL verifyRefreshCRL(Path file, X509CRL crl, Set<TrustAnchor> ta)
            throws IOException, CRLException, CertificateException, NoSuchProviderException {
        Date now = new Date();
        if (crl.getNextUpdate().compareTo(now) < 0) {
            Path urlf = getCRLFileURLFile(file.toString());
            URL url = readUrlFromFile(urlf);
            if (url != null) {
                crl = downloadCRL(url, ta);
                // remove CRL only if we can get new CRL instead
                if (crl != null) {
                    Files.deleteIfExists(urlf);
                    Files.deleteIfExists(file);
                    logger.debug("Removed CRL file: {}", file);
                    logger.debug("Removed CRL url file: {}", urlf);
                }
            } else {
                crl = null;
                Files.deleteIfExists(file);
                logger.debug("Removed CRL file: {}", file);
            }
        }
        return crl;
    }

    private X509CRL downloadCRL(URL url, Set<TrustAnchor> ta)
            throws IOException, CRLException, CertificateException, NoSuchProviderException {
        synchronized (lock) {
            logger.debug("downloading CRL from {}", url);
            final Path crlDir = getCRLLoc();
            if (!Files.exists(crlDir))
                Files.createDirectory(crlDir);
            HttpURLConnection crlConn = (HttpURLConnection) url.openConnection();
            crlConn.setDoInput(true);
            crlConn.setUseCaches(false);
            crlConn.setDefaultUseCaches(false);
            int respCode = crlConn.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
                String name = crlDir.toString() + File.separator + sdf.format(new Date());
                Path crlf = Paths.get(name);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (InputStream is = new BufferedInputStream(crlConn.getInputStream());
                     OutputStream os = new BufferedOutputStream(Files.newOutputStream(crlf))) {
                    byte[] respBuffer = new byte[8192];
                    int count, size = 0;
                    int limit = 20 * 1024 * 1024;
                    boolean exceeded;
                    while ((count = is.read(respBuffer)) >= 0) {
                        os.write(respBuffer, 0, count);
                        bos.write(respBuffer, 0, count);
                        size += count;
                        exceeded = size > limit;
                        if (exceeded)
                            return null;
                    }
                }
                CertificateFactory cf = CertificateFactory.getInstance("X.509", KalkanProvider.PROVIDER_NAME);
                X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(bos.toByteArray()));
                if (verifyCRL(crl, ta)) {
                    Files.move(crlf, Paths.get(name + ".crl"));
                    try (BufferedWriter w = Files.newBufferedWriter(Paths.get(name + ".url"), StandardCharsets.UTF_8)) {
                        w.write(url.toString());
                    }
                    logger.debug("saved CRL file {}.crl", name);
                    return crl;
                } else {
                    logger.error("removed downloaded CRL file {}", name);
                    Files.deleteIfExists(crlf);
                }
            } else {
                logger.error("Can't download CRL from {}. HTTP code: {}", url, respCode);
            }
            return null;
        }
    }

    private void downloadCRLs(List<URI> urls, Set<TrustAnchor> ta) {
        if (urls == null || urls.isEmpty())
            return;
        for (URI uri : urls) {
            try {
                X509CRL crl = downloadCRL(uri.toURL(), ta);
                if (crl == null)
                    continue;
                crlset.add(crl);
                break;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean verifyCRL(X509CRL crl, Set<TrustAnchor> ta) {
        Date now = new Date();
        if (crl.getThisUpdate().compareTo(now) > 0 || crl.getNextUpdate().compareTo(now) < 0) {
            logger.error("CRL period is invalid: {} - {}", crl.getThisUpdate(), crl.getNextUpdate());
            return false;
        } else {
            logger.trace("CRL period: {} - {}", crl.getThisUpdate(), crl.getNextUpdate());
        }
        try {
            List<? extends Certificate> caCertList = certPathUtil.getCertPathCAList(crl.getIssuerX500Principal(), ta);
            for (Certificate certificate : caCertList) {
                X509Certificate cert = (X509Certificate) certificate;
                try {
                    crl.verify(cert.getPublicKey(), KalkanProvider.PROVIDER_NAME);
                    return true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        logger.error("CRL verification failed");
        return false;
    }

    private static Path getCRLFileURLFile(String fileName) throws IOException {
        int pos = fileName.lastIndexOf('.');
        if (pos > -1) {
            String name = fileName.substring(0, pos);
            Path urlFile = Paths.get(name + ".url");
            if (Files.exists(urlFile)) {
                return urlFile;
            }
        }
        return null;
    }

    private static URL readUrlFromFile(Path urlFile) throws IOException {
        if (urlFile != null) {
            try (BufferedReader r = Files.newBufferedReader(urlFile, StandardCharsets.UTF_8)) {
                return new URL(r.readLine());
            }
        }
        return null;
    }

    private List<URI> getCRLDistributionPoints(X509Certificate cert) throws IOException, URISyntaxException {
        byte[] crldpExt = cert.getExtensionValue(X509Extensions.CRLDistributionPoints.getId());

        if (crldpExt == null)
            return Collections.emptyList();
        List<URI> crlUrls = new LinkedList<>();
        ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
        DERObject derObjCrlDP = oAsnInStream.readObject();
        oAsnInStream.close();
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
        DERObject derObj2 = oAsnInStream2.readObject();
        oAsnInStream2.close();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for an URI
                for (GeneralName genName : genNames) {
                    if (genName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                        URI url = new URI(DERIA5String.getInstance(genName.getName()).getString());
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;

    }

    private List<URI> getDeltaCRLDistributionPoints(X509Certificate cert) throws URISyntaxException, IOException {
        byte[] freshestCrlValue = cert.getExtensionValue(X509Extensions.FreshestCRL.getId());
        if (null == freshestCrlValue)
            return Collections.emptyList();
        List<URI> deltaCrlUris = new LinkedList<>();
        ASN1Sequence seq;
        DEROctetString oct;
        ASN1InputStream stream = new ASN1InputStream(new ByteArrayInputStream(freshestCrlValue));
        oct = (DEROctetString) stream.readObject();
        stream.close();
        stream = new ASN1InputStream(oct.getOctets());
        seq = (ASN1Sequence) stream.readObject();
        stream.close();
        CRLDistPoint distPoint = CRLDistPoint.getInstance(seq);
        DistributionPoint[] distributionPoints = distPoint.getDistributionPoints();
        for (DistributionPoint distributionPoint : distributionPoints) {
            DistributionPointName distributionPointName = distributionPoint.getDistributionPoint();
            if (DistributionPointName.FULL_NAME != distributionPointName.getType()) {
                continue;
            }
            GeneralNames generalNames = (GeneralNames) distributionPointName.getName();
            GeneralName[] names = generalNames.getNames();
            for (GeneralName name : names) {
                if (name.getTagNo() != GeneralName.uniformResourceIdentifier) {
                    continue;
                }
                DERIA5String derStr = DERIA5String.getInstance(name.getDERObject());
                String str = derStr.getString();
                URI uri = new URI(str);
                deltaCrlUris.add(uri);
            }
        }
        return deltaCrlUris;
    }

    private static BigInteger getDeltaCrlNumber(X509CRL CRL) {
        return getCrlExtension(CRL, X509Extensions.DeltaCRLIndicator.getId()); // "2.5.29.27"
    }

    private static BigInteger getCrlExtension(X509CRL crl, String extensionOID) {
        byte[] extensionValue = crl.getExtensionValue(extensionOID);
        if (extensionValue == null)
            return null;

        try {
            ASN1InputStream as = new ASN1InputStream(new ByteArrayInputStream(extensionValue));
            DEROctetString octetString = (DEROctetString) (as.readObject());
            as.close();
            byte[] octets = octetString.getOctets();
            as = new ASN1InputStream(octets);
            DERInteger integer = (DERInteger) as.readObject();
            as.close();
            return integer.getPositiveValue();
        } catch (IOException e) {
            return null;
        }
    }
}
