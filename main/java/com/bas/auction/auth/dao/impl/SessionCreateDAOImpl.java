package com.bas.auction.auth.dao.impl;

import com.bas.auction.auth.dao.SessionCreateDAO;
import com.bas.auction.auth.dao.SessionDAO;
import com.bas.auction.auth.dto.Session;
import com.bas.auction.core.Conf;
import com.bas.auction.core.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Repository;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class SessionCreateDAOImpl implements SessionCreateDAO {
    private final SessionDAO sessionDAO;
    public static final String SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY = "x-auth-token";
    public static final String DEFAULT_PARAMETER = "remember-me";
    public static final int TWO_WEEKS_S = 1209600;
    private String cookieName = SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;
    private int tokenValiditySeconds = TWO_WEEKS_S;
    private final Conf conf;
    private static final String DELIMITER = ":";
    private final static Logger logger = LoggerFactory.getLogger(SessionCreateDAOImpl.class);

    @Autowired
    public SessionCreateDAOImpl(SessionDAO sessionDAO, Conf conf) {
        this.sessionDAO = sessionDAO;
        this.conf = conf;
    }


    public void createSession(long userId, HttpServletRequest request, HttpServletResponse response) {

        Session session = new Session();
        String userAgent = request.getHeader("User-Agent");
        String ip = Utils.getClientIpAddr(request);
        String authToken = UUID.randomUUID().toString();
        session.setUserId(userId);
        session.setAuthToken(authToken);
        session.setUserAgent(userAgent);
        session.setIpAddress(ip);
        sessionDAO.create(session);
        setCookie(new String[]{authToken}, getTokenValiditySeconds(), request, response);

    }



    public void set_var (long userId){
        Object[] new_params;
        Long n_session= sessionDAO.sessionIdByUser(userId);
        logger.info("n_session="+n_session);
        new_params = new Object[2];
        new_params[0]="session_id";
        new_params[1]=n_session.toString();
        sessionDAO.set_session_id(new_params);
    }



    protected String encodeCookie(String[] cookieTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookieTokens.length; i++) {
            sb.append(cookieTokens[i]);

            if (i < cookieTokens.length - 1) {
                sb.append(DELIMITER);
            }
        }

        String value = sb.toString();

        sb = new StringBuilder(new String(Base64.encode(value.getBytes())));

        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
    protected void setCookie(String[] tokens, int maxAge, HttpServletRequest request,
                             HttpServletResponse response) {
        String cookieValue = encodeCookie(tokens);
        Cookie cookie = new Cookie(getCookieName(), cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setDomain(getCookieDomain());
        if (maxAge < 1) {
            cookie.setVersion(1);
        }
        response.addCookie(cookie);
    }

    protected String getCookieName() {
        return cookieName;
    }



    private String getCookieDomain() {
        String host = conf.getHost();
        String rootHost = conf.getRootHost();
        if (rootHost == null)
            rootHost = host;
        try {
            URL url = new URL(rootHost);
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }


    protected int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }




}
