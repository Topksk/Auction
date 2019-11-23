package com.bas.auction.core.crypto;

import com.bas.auction.auth.dao.UserDAO;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.impl.UserCertInfoServiceImpl;
import com.bas.auction.core.crypto.CertValidationError.Type;
import com.bas.auction.core.utils.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

@Component
public class CertValidation {
    private final static Logger logger = LoggerFactory.getLogger(CertValidation.class);
    private final CRLUtil crlUtil;
    private final CertUtil certUtil;
    private final CertPathUtil certPathUtil;
    private final UserDAO userDAO;
    private final UserCertInfoServiceImpl userCertInfoService;

    @Autowired
    public CertValidation(CRLUtil crlUtil, CertUtil certUtil, CertPathUtil certPathUtil, UserDAO userDAO, UserCertInfoServiceImpl userCertInfoService) {
        this.crlUtil = crlUtil;
        this.certUtil = certUtil;
        this.certPathUtil = certPathUtil;
        this.userDAO = userDAO;
        this.userCertInfoService = userCertInfoService;
    }

    public void validate(X509Certificate cert) throws CertValidationError {
        logger.trace("start cert validation: {}", cert.getSubjectX500Principal().toString());
        validatePeriod(cert);
        Set<TrustAnchor> trustAnchors = certUtil.getTrustAnchors();
        PKIXCertPathValidatorResult path = certPathUtil.validatePath(cert, trustAnchors);
        boolean isNotResident = certUtil.isNotResident(path);
        if (isNotResident)
            logger.debug("non resident");
        else
            logger.debug("resident");
        if (crlUtil.isRevoked(cert, trustAnchors))
            throw new CertValidationError(Type.REVOKED);
        validateCertFormat(cert, isNotResident);
        logger.trace("finished cert validation: {}", cert.getSubjectX500Principal().toString());
    }

    public void validate(Long userId, X509Certificate cert) throws CertValidationError {
        validate(cert);
        Entry<String, String> userIinAndBin = userDAO.findUserIinAndBin(userId);
        UserCertInfo userCertInfo = userCertInfoService.getUserCertInfo(cert);
        boolean iinEquals = Objects.equals(userIinAndBin.getKey(), userCertInfo.getIin());
        if (!iinEquals)
            throw new CertValidationError(Type.AUTH_DS_IIN_MISMATCH);
        boolean binEquals = Objects.equals(userIinAndBin.getValue(), userCertInfo.getBin());
        if (!binEquals)
            throw new CertValidationError(Type.AUTH_DS_BIN_MISMATCH);
    }

    public void validatePeriod(X509Certificate cert) throws CertValidationError {
        try {
            cert.checkValidity();
            logger.trace("cert period: {} - {}", cert.getNotBefore(), cert.getNotAfter());
        } catch (CertificateExpiredException e) {
            logger.error("cert expired: {} - {}", cert.getNotBefore(), cert.getNotAfter());
            throw new CertValidationError(Type.VALIDITY_EXPIRED);
        } catch (CertificateNotYetValidException e) {
            logger.error("cert not yet valid: {} - {}", cert.getNotBefore(), cert.getNotAfter());
            throw new CertValidationError(Type.NOT_YET_VALID);
        }
    }

    public void validateCertFormat(X509Certificate cert, boolean notResident) throws CertValidationError {
        Map<String, String> sn = certUtil.getCertSubjectTokens(cert);
        boolean resident = !notResident;
        if (resident) {
            if (sn.get("SERIALNUMBER") == null) {
                logger.error("IIN absent");
                throw new CertValidationError(Type.IIN_ABSENCE);
            } else {
                String iin = sn.get("SERIALNUMBER").replaceAll("[^\\d]", "");
                if (!Validator.isValidIinOrBin(iin)) {
                    logger.error("invalid IIN: {}", iin);
                    throw new CertValidationError(Type.IIN_VALIDATION);
                }
            }
            if (sn.get("OU") != null) {
                String bin = sn.get("OU").replaceAll("[^\\d]", "");
                if (!Validator.isValidIinOrBin(bin)) {
                    logger.error("invalid bin: {}", bin);
                    throw new CertValidationError(Type.BIN_VALIDATION);
                }
            }
        } else {
            if (sn.get("SERIALNUMBER") == null) {
                logger.error("non resident org id absent");
                throw new CertValidationError(Type.NON_RES_ORG_ID_ABSENCE);
            }
            if (sn.get("EMAILADDRESS") == null) {
                logger.error("non resident email absent");
                throw new CertValidationError(Type.EMAIL_ABSENCE);
            }
        }

        if (sn.get("CN") == null || "".equals(sn.get("CN").trim())) {
            logger.error("CN field absent");
            throw new CertValidationError(Type.FIRST_LAST_NAME_ABSENCE);
        } else if (resident && sn.get("CN").trim().replaceAll("\\s+", " ").split(" ").length == 1) {
            logger.error("CN field conteins only one word");
            throw new CertValidationError(Type.FIRST_LAST_NAME_VALIDATION);
        }
    }
}
