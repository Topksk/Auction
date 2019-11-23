package com.bas.auction.core.config.security;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserAuthInfo.RegStatus;
import com.bas.auction.core.Conf;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.req.dao.RequestDAO;
import com.bas.auction.req.draft.service.ReqDraftService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final Conf conf;
    private final CustomerDAO customerDAO;
    private final SupplierDAO supplierDAO;
    private final AuthDAO authDAO;
    private final RequestDAO reqDAO;
    private final com.bas.auction.req.draft.service.ReqDraftService ReqDraftService;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessHandlerImpl.class);

    public AuthenticationSuccessHandlerImpl(Conf conf, CustomerDAO customerDAO, SupplierDAO supplierDAO, AuthDAO authDAO, RequestDAO reqDAO, ReqDraftService ReqDraftService ) {
        this.conf = conf;
        this.customerDAO = customerDAO;
        this.supplierDAO = supplierDAO;
        this.authDAO = authDAO;
        this.reqDAO = reqDAO;
        this.ReqDraftService=ReqDraftService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(authentication);
        String err = null;
        Map params = new HashMap <String, Object>();
        User user = (User) authentication.getPrincipal();
        String additional = authDAO.findAuthMain(user.getEmail());
        Long posType = authDAO.findPosition(user.getEmail());
        params.put("userlogin", user.getEmail());
        params.put("sqlpath", "upd_t_enter_log");
        String authUrl=request.getParameter("authurl");
        if (posType!=0 || (additional.indexOf("notexists")>0)){
            targetUrl="login.html";
        }

         try {
            err = ReqDraftService.creates(params, user);
            }catch (Exception e){

            };

        if ((!authUrl.equals("login.html")) && (!authUrl.equals("user_reg.html")) && (!authUrl.isEmpty()) ){
            redirectStrategy.sendRedirect(request, response, authUrl);
        }else{
            //if (additional.indexOf("notexists")>0 && posType==0){
                //redirectStrategy.sendRedirect(request, response, targetUrl + "#ADDRESSPOPUP");
            //    redirectStrategy.sendRedirect(request, response, targetUrl);
            //}else {
                if (additional.substring(0,1).equals("2")) {
                    redirectStrategy.sendRedirect(request, response,  "#REQUESTPOPUP");
                } else {
                    redirectStrategy.sendRedirect(request, response, targetUrl);
                }
            //}
        }

            clearAuthenticationAttributes(request);
    }

    private String determineTargetUrl(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String targetUrl = null;
        String host = conf.getHost();
        /*if (user.isSysadmin())
            targetUrl = "admin.html";
        else if (user.isCustomer())
            targetUrl = determineCustomerUserTargetUrl(user);
        else if (user.isSupplier())
            targetUrl = determineSupplierUserTargetUrl(user);
        if(targetUrl != null)
            targetUrl = host + "/" + targetUrl;
        else
            targetUrl = conf.getLoginUrl();*/

        targetUrl="notifforuser.html";
        return targetUrl;
    }

    private String determineCustomerUserTargetUrl(User user) {
        String targetUrl = null;
        String regStatus = customerDAO.findRegStatus(user.getCustomerId());
        if (regStatus != null) {
            RegStatus status = RegStatus.valueOf(regStatus);
            if (status == RegStatus.APPROVED)
                targetUrl = "customer.html";
            else if (user.isMainUser() && status == RegStatus.AGREEMENT_NOT_SIGNED)
                targetUrl = "customerreg.html";
        }
        return targetUrl;
    }

    private String determineSupplierUserTargetUrl(User user) {
        String targetUrl = null;
        String regStatus = supplierDAO.findRegStatus(user.getSupplierId());
        if (regStatus != null) {
            RegStatus status = RegStatus.valueOf(regStatus);
                if (status == RegStatus.APPROVED)
                targetUrl = "supplier.html";
            else if (user.isMainUser()
                    && (status == RegStatus.IN_PROGRESS || status == RegStatus.REJECTED))
                targetUrl = "supplierreg.html";
        }
        return targetUrl;
    }

    /**
     * Removes temporary authentication-related data which may have been stored in the
     * session during the authentication process.
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
