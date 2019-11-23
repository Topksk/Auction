package com.bas.auction.docfiles.dao.impl;

import com.bas.auction.core.Conf;
import com.bas.auction.core.crypto.CertValidationError;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.UserCertificateDAO;
import com.bas.auction.docfiles.dto.UserCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Repository
public class UserCertificateDAOImpl implements UserCertificateDAO, GenericDAO<UserCertificate> {

    private final static Logger logger = LoggerFactory.getLogger(UserCertificateDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final Conf conf;

    @Autowired
    public UserCertificateDAOImpl(DaoJdbcUtil daoutil, Conf conf) {
        this.daoutil = daoutil;
        this.conf = conf;
    }

    @Override
    public String getSqlPath() {
        return "cert";
    }

    @Override
    public Class<UserCertificate> getEntityType() {
        return UserCertificate.class;
    }

    @Override
    @SpringTransactional
    public Long create(Long userId, X509Certificate cert) throws IOException, CertificateException {
        String serialNum = cert.getSerialNumber().toString(16).toLowerCase();
        Object[] values = {userId, serialNum};
        KeyHolder kh = daoutil.insert(this, values);
        Long id = (Long) kh.getKeys().get("certificate_id");
        logger.debug("stored user certificate in db: certId = {}", id);
        String certPath = conf.getUserCertsStorePath() + id + ".cer";
        Path certFile = Paths.get(certPath);
        logger.debug("storing user certificate in file: userId = {}, certPath = {}", userId, certPath);
        Files.write(certFile, cert.getEncoded());
        return id;
    }

    @Override
    public Long findCert(Long userId, X509Certificate cert) throws CertValidationError, IOException, CertificateException {
        String serialNum = cert.getSerialNumber().toString(16).toLowerCase();
        return daoutil.queryScalar(this, "find_id_by_serialnum", userId, serialNum);
    }

    @Override
    public Long findUserId(Long certId) {
        return daoutil.queryScalar(this, "find_userid", certId);
    }
}