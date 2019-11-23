package com.bas.auction.core.config;

import com.bas.auction.auth.dto.User;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

public class MDCFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.clear();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                if (principal instanceof User)
                    MDC.put("uid", String.valueOf(((User) principal).getUserId()));
                else
                    MDC.put("uid", String.valueOf(principal));
            }
        }
        chain.doFilter(request, response);
        MDC.clear();
    }

    @Override
    public void destroy() {
        MDC.clear();
    }
}
