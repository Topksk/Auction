package com.bas.auction.digsign.impl;

import com.bas.auction.core.crypto.CertUtil;
import com.bas.auction.core.crypto.CertValidation;
import com.bas.auction.core.crypto.ZipContent;
import com.bas.auction.core.utils.ZipUtils;
import com.bas.auction.digsign.DigitalSignatureArchiveProcessorService;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dao.UserCertificateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Map;

@Service
public class DigitalSignatureArchiveProcessorServiceImpl implements DigitalSignatureArchiveProcessorService {
    private final static Logger logger = LoggerFactory.getLogger(DigitalSignatureArchiveProcessorServiceImpl.class);
    private final DocFileDAO docFileDAO;
    private final DocFileAttributeDAO docFileAttributeDAO;
    private final UserCertificateDAO userCertificateDAO;
    private final CertValidation certValidation;
    private final CertUtil certUtil;
    private final DocFileSignatureDAO docFileSignatureDAO;

    @Autowired
    public DigitalSignatureArchiveProcessorServiceImpl(DocFileDAO docFileDAO, DocFileAttributeDAO docFileAttributeDAO,
                                                       UserCertificateDAO userCertificateDAO,
                                                       CertValidation certValidation, CertUtil certUtil, DocFileSignatureDAO docFileSignatureDAO) {
        this.docFileDAO = docFileDAO;
        this.docFileAttributeDAO = docFileAttributeDAO;
        this.userCertificateDAO = userCertificateDAO;
        this.certValidation = certValidation;
        this.certUtil = certUtil;
        this.docFileSignatureDAO = docFileSignatureDAO;
    }

    @Override
    public void processArchiveWithSignedFile(HttpServletRequest req, long userId) throws Exception {
        Part filePart = null;
        ZipUtils zipUtils = new ZipUtils();
        try {
            filePart = req.getPart("file1");
            Map<String, String> attributes = processAttributes(req);
            ZipContent zipContent = zipUtils.unzipArchiveWithSignedFile(filePart);
            Long certId = processCertificate(userId, zipContent.certificateFile);
            long fileId = docFileDAO.create(zipContent.fileName, userId, false, zipContent.file, attributes);
            docFileSignatureDAO.create(fileId, certId, zipContent.signatureFile);
        } finally {
            zipUtils.deleteTmpFiles();
            if (filePart != null)
                filePart.delete();
        }
    }

    @Override
    public void processArchiveWithSignature(HttpServletRequest req, long userId) throws Exception {
        Part filePart = null;
        ZipUtils zipUtils = new ZipUtils();
        try {
            filePart = req.getPart("file1");
            Map<String, String> attributes = processAttributes(req);
            Long fileId = Long.valueOf(attributes.get("fileId"));
            ZipContent zipContent = zipUtils.unzipArchiveWithSignature(filePart);
            Long certId = processCertificate(userId, zipContent.certificateFile);
            docFileSignatureDAO.create(fileId, certId, zipContent.signatureFile);
        } finally {
            zipUtils.deleteTmpFiles();
            if (filePart != null)
                filePart.delete();
        }
    }

    @Override
    public void processArchiveWithUnsignedFile(HttpServletRequest req, long userId) throws Exception {
        Part filePart = null;
        ZipUtils zipUtils = new ZipUtils();
        try {
            filePart = req.getPart("file1");
            Map<String, String> attributes = processAttributes(req);
            ZipContent zipContent = zipUtils.unzipArchiveWithUnsignedFile(filePart);
            logger.debug("file name: {}", zipContent.fileName);
            docFileDAO.create(zipContent.fileName, userId, false, zipContent.file, attributes);
        } finally {
            zipUtils.deleteTmpFiles();
            if (filePart != null)
                filePart.delete();
        }
    }

    private Map<String, String> processAttributes(HttpServletRequest req) {
        String attr = req.getParameter("fileAttributes");
        Map<String, String> attributes = null;
        if (attr != null)
            attributes = docFileAttributeDAO.parse(attr);
        logger.debug("attributes: {}", attributes);
        return attributes;
    }

    private Long processCertificate(long userId, Path certificateFile) throws Exception {
        X509Certificate cert = certUtil.readCertificateFromFile(certificateFile);
        certValidation.validate(userId, cert);
        Long certId = userCertificateDAO.findCert(userId, cert);
        if (certId == null)
            certId = userCertificateDAO.create(userId, cert);
        return certId;
    }
}
