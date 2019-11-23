package com.bas.auction.core.config.security;

import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.AuthService;
import com.bas.auction.auth.service.UserCertInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class UsernamePasswordAuthenticationFilterExt extends UsernamePasswordAuthenticationFilter {
    private final static Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthenticationFilterExt.class);
    private final UserCertInfoService userCertInfoDAO;
    private final AuthService authService;

    public UsernamePasswordAuthenticationFilterExt(UserCertInfoService userCertInfoDAO, AuthService authService) {
        super();
        this.userCertInfoDAO = userCertInfoDAO;
        this.authService = authService;
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {

        String login, role;
        role=request.getParameter("role").toLowerCase();
        login = authService.findAdminLogin(role);
        return login;
    }

    private X509Certificate getRequestCert(HttpServletRequest req) {
        try {
            X509Certificate userCert = getRequestCertFromHeader(req);
            if (userCert != null)
                return userCert;
            String cipherSuite = (String) req.getAttribute("javax.servlet.request.cipher_suite");
            if (cipherSuite != null) {
                X509Certificate[] certChain = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
                if (certChain == null)
                    return null;
                else
                    return certChain[0];
            }
        } catch (CertificateException e) {
            logger.debug("error extracting certificate", e);
        }
        return null;
    }

    private X509Certificate getRequestCertFromHeader(HttpServletRequest req) throws CertificateException {
        String clientCert = req.getHeader("SSL_CLIENT_CERT");
        if (clientCert != null && !"".equals(clientCert)) {
            clientCert = clientCert.trim().replaceAll("\t", "\n").trim();
            InputStream is = new ByteArrayInputStream(clientCert.getBytes(StandardCharsets.UTF_8));
            CertificateFactory cf1 = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf1.generateCertificate(is);
        } else
            return null;
    }
}
