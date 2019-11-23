package com.bas.auction.core.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        authException.printStackTrace();
        if (authException.toString().indexOf("InsufficientAuthenticationException")>0){
            response.sendRedirect(request.getContextPath() + "401.html");
        }else{
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
