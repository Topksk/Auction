package com.bas.auction.core.config.security;

import com.bas.auction.auth.dao.SessionDAO;
import com.bas.auction.auth.dto.Session;
import com.bas.auction.auth.dto.User;
import com.bas.auction.core.Conf;
import com.bas.auction.core.utils.Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class JdbcTokenBasedRememberMeServicesImpl extends AbstractRememberMeServices {
    private final Conf conf;
    private final SessionDAO sessionDAO;

    public JdbcTokenBasedRememberMeServicesImpl(Conf conf, String key,
                                                UserDetailsService userDetailsService,
                                                int tokenValiditySeconds,
                                                SessionDAO sessionDAO) {
        super(key, userDetailsService);
        this.conf = conf;
        this.sessionDAO = sessionDAO;
        setAlwaysRemember(true);
        setTokenValiditySeconds(tokenValiditySeconds);
        setUseSecureCookie(true);
        setCookieName(SessionDAO.REMEMBER_ME_COOKIE_KEY);
    }

    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        User user = getUser(successfulAuthentication);
        if (user == null)
            return;
        Session session = new Session();
        String userAgent = request.getHeader("User-Agent");
        String ip = Utils.getClientIpAddr(request);
        String authToken = UUID.randomUUID().toString();
        session.setUserId(user.getUserId());
        session.setAuthToken(authToken);
        session.setUserAgent(userAgent);
        session.setIpAddress(ip);
        sessionDAO.create(session);
        setCookie(new String[]{authToken}, getTokenValiditySeconds(), request, response);
    }

    @Override
    protected UserDetails processAutoLoginCookie(String[] cookieTokens, HttpServletRequest request, HttpServletResponse response) throws RememberMeAuthenticationException, UsernameNotFoundException {
        Session session = sessionDAO.findByToken(cookieTokens[0]);
        if (session == null)
            throw new UsernameNotFoundException("");
        return session.getUser();
    }

    @Override
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

    @Override
    protected void cancelCookie(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Cancelling cookie");
        Cookie cookie = new Cookie(getCookieName(), null);
        cookie.setMaxAge(0);
        cookie.setVersion(1);
        cookie.setPath("/");
        cookie.setDomain(getCookieDomain());
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        super.logout(request, response, authentication);
        User user = getUser(authentication);
        if (user == null)
            return;
        logger.debug("Logout user sess{}"+user.getUserId());
        sessionDAO.deactivateUserSessions(user.getUserId());
    }

    private User getUser(Authentication authentication) {
        if (authentication == null)
            return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof User)
            return (User) principal;
        return null;
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
}
