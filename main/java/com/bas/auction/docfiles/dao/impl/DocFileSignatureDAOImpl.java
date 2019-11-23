package com.bas.auction.docfiles.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dao.SignatureTooBigException;
import com.bas.auction.docfiles.dao.UserCertificateDAO;
import com.bas.auction.docfiles.dto.DocFileSignature;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class DocFileSignatureDAOImpl implements DocFileSignatureDAO, GenericDAO<DocFileSignature> {
    private final Logger logger = LoggerFactory.getLogger(DocFileSignatureDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final UserCertificateDAO certDAO;

    @Autowired
    public DocFileSignatureDAOImpl(DaoJdbcUtil daoutil, UserCertificateDAO certDAO) {
        this.daoutil = daoutil;
        this.certDAO = certDAO;
    }

    @Override
    public String getSqlPath() {
        return "signature";
    }

    @Override
    public Class<DocFileSignature> getEntityType() {
        return DocFileSignature.class;
    }

    @Override
    public byte[] findSignatureBody(Long signId) {
        byte[] body = null;
        String signature = daoutil.queryScalar(this, "get_signature_body", signId);
        if (signature != null)
            body = Base64.decodeBase64(signature);
        else
            logger.warn("signature not found: {}", signId);
        return body;
    }

    @Override
    public List<DocFileSignature> findFileSignatures(Long fileId) {
        return daoutil.query(this, "list", fileId);
    }

    @Override
    @SpringTransactional
    public void create(DocFileSignature fileSignature) {
        Object[] values = getInsertValues(fileSignature);
        daoutil.insert(this, values);
    }

    @Override
    @SpringTransactional
    public void create(List<DocFileSignature> fileSignatures) {
        List<Object[]> values = fileSignatures.stream().map(this::getInsertValues).collect(toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] getInsertValues(DocFileSignature fileSignature) {
        return new Object[] {fileSignature.getFileId(), fileSignature.getCertificateId(), fileSignature.getSignature(),
                fileSignature.getCreatedBy(), fileSignature.getCreatedBy()};
    }

    @Override
    @SpringTransactional
    public void create(Long fileId, Long certId, Path file) throws IOException {
        long size = Files.size(file);
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (size > freeMemory / 2)
            throw new SignatureTooBigException();
        // signature is small file, so OutOfMemory should not throw here
        try (InputStream fis = Files.newInputStream(file)) {
            byte[] tmp = new byte[(int) size];
            int i = fis.read(tmp);
            if (i < tmp.length)
                tmp = Arrays.copyOf(tmp, i);
            String encodedSignature = Base64.encodeBase64String(tmp);
            Long userId = certDAO.findUserId(certId);
            DocFileSignature fileSignature = new DocFileSignature();
            fileSignature.setFileId(fileId);
            fileSignature.setCertificateId(certId);
            fileSignature.setSignature(encodedSignature);
            fileSignature.setCreatedBy(userId);
            create(fileSignature);
        }
    }

    @Override
    @SpringTransactional
    public void deleteFileSignatures(Long fileId) {
        Object[] values = {fileId};
        daoutil.delete(this, values);
    }
}