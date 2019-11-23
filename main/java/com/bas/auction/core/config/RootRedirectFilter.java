package com.bas.auction.core.config;

import com.bas.auction.core.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RootRedirectFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(RootRedirectFilter.class);
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final Conf conf;

    public RootRedirectFilter(Conf conf) {
        this.conf = conf;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestURI = req.getRequestURI();
        if ("/".equals(requestURI)) {
            logger.debug("redirecting to login page");
            redirectStrategy.sendRedirect(req, (HttpServletResponse) response, conf.getLoginUrl());
        } else
            chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
