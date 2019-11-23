package com.bas.auction.auth.controller;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dao.SessionCreateDAO;
import com.bas.auction.auth.dao.UserDAO;
import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.AuthService;
import com.bas.auction.auth.service.UserCertInfoService;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/auth", produces = APPLICATION_JSON_UTF8_VALUE)
public class AuthController extends RestControllerExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final MessageDAO messageDAO;
    private final AuthService authService;
    private final UserCertInfoService userCertInfoDAO;
    private final UserService userService;
    private final CryptoUtils cryptoUtils;
    private final Conf conf;
    private final SupplierService supplierService;
    private final EmployeeService employeeService;
    private final UserDAO userDAO;
    private final SessionCreateDAO sessionCreateDAO;
    private final AuthDAO authDAO;

    @Autowired
    public AuthController(MessageDAO messageDAO, AuthService authService, UserCertInfoService userCertInfoDAO, UserService userService, CryptoUtils cryptoUtils, Conf conf, SupplierService supplierService, EmployeeService employeeService, UserDAO userDAO, SessionCreateDAO sessionCreateDAO, AuthDAO authDAO) {
        super(messageDAO);
        this.messageDAO = messageDAO;
        this.authService = authService;
        this.userCertInfoDAO = userCertInfoDAO;
        this.userService = userService;
        this.cryptoUtils = cryptoUtils;
        this.conf = conf;
        this.supplierService = supplierService;
        this.employeeService = employeeService;
        this.userDAO = userDAO;
        this.sessionCreateDAO = sessionCreateDAO;
        this.authDAO = authDAO;
    }

    @RequestMapping(params = "user_info", method = GET)
    public Map<String, Object> getUserInfo(HttpServletRequest request) throws CertificateException {
        MDC.put("action", "login");

    //    X509Certificate cert = getRequestCert(request);
     //   UserCertInfo certInfo = userCertInfoDAO.findWithExistCheck(cert);
        String  role=request.getParameter("role");
        return authService.findAuthInfo(role);
    }

    @RequestMapping(params = "cert_info", method = GET)
    public UserCertInfo getCertInfo(HttpServletRequest req) throws CertificateException {
        MDC.put("action", "cert info");
        X509Certificate cert = getRequestCert(req);
        return userCertInfoDAO.findWithExistCheck(cert);
    }

    @RequestMapping(params = "activate_email", method = POST)
    public void activateEmail(@RequestParam String code,
                              @CurrentUser User user,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        MDC.put("action", "activate email");
        /*if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }
*/
        logger.debug("A");
        long userId=userService.activateEmail(code);
        sessionCreateDAO.createSession(userId,request, response );
    }

    @RequestMapping(params = "reset_password", method = POST)
    public void resetPassword(@RequestParam String code,
                              @RequestParam String pass,
                              @CurrentUser User user,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        MDC.put("action", "reset password");
        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }
        String params = "#PASSWORD_RESET_SUCCESS";
        try {
            userService.resetPassword(code, pass);
        } catch (ApplException e) {
            params = "?result=" + e.getCodes().get(0);
        }
        String targetUrl = conf.getLoginUrl() + params;
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    @RequestMapping(params = "change_password", method = POST)
    public void changePassword(@RequestBody Map<String, Object> params,
                               @CurrentUser User user) throws IOException {
        MDC.put("action", "change password");
        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }
        if (!user.getLogin().equals("demo@topksk.kz")&&!user.getLogin().equals("akimat1@topksk.kz")) {
            if (params.get("mypass") == null) {
                userService.newPassword(user.getUserId(), (String) params.get("newpass"));
            } else {

                userService.changePassword(user.getUserId(), (String) params.get("mypass"), (String) params.get("newpass"));
            }
        }
        String targetUrl = conf.getLoginUrl() + params;
      //  redirectStrategy.sendRedirect(request, response, targetUrl);




    }

    @RequestMapping(params = "check_auths", method = POST)
    public String checkAuths(@RequestBody Map<String, Object> params,  @CurrentUser User user) throws IOException {
        MDC.put("action", "check_auths");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        //String pswd=

        /*logger.debug("pswd=="+pswd);
        if (pswd.equals("no_psw_found")){
            String email = userService.sendPasswordResetMail((String) params.get("login"));
            return pswd;
        }else {*/

        String pwdHash = userDAO.findPasswordHashByEmail((String) params.get("login"));
        logger.debug("pwdHash=="+pwdHash);
        if (pwdHash==null){
            String email = userService.sendPasswordResetMail((String) params.get("login"));
            logger.debug("AuthController="+email);
            if (email.equals("email_not_found")){
                return "email_not_found";
            }else {
            return "no_psw_found";
            }

        }else{
        userService.checkPasswordByEmail( (String) params.get("login"), (String) params.get("pass"));
            return (String) params.get("login");
        }

    }


    @RequestMapping(params = "check_auths_serv", method = POST)
    public String checkServContact(@RequestBody Map<String, Object> params) throws IOException {
        MDC.put("action", "check_auths_serv");
        Map<String, Object> params2=new HashMap<>();
        List<Map<String, Object>> res;
        String resMess="";
        params2.put("sqlpath", "auth/check_serv_comp_by_iinbin" );
        params2.put("iin_bin", (String) params.get("iinbins") );

        res=authDAO.findOther(params2);
        if(res.size()>0) {
            resMess="compexists";
            logger.info("iiiiiiipppp=" + (String) res.get(0).get("iin_bin"));
        }
        if (userDAO.findCustomerUserInfoForEmailNotif((String) params.get("login"))==null){
            logger.error("no user email found checkServContact");
            resMess+=" email_not_found";
            return resMess;
        }else {

            try {
                userService.checkPasswordByEmail((String) params.get("login"), (String) params.get("pass"));
            } catch (Exception e) {
                resMess+=" invalid_password";
                return resMess;
        }
        }

        resMess+=(String) params.get("login");
            return resMess;

    }



    @RequestMapping(params = "check_auths_sn", method = POST)
    public String checkAuths_sn(@RequestBody Map<String, Object> params,
                                @CurrentUser User user,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        MDC.put("action", "check_auths_sn");

        Long userId=authDAO.findUserIdByEmail((String) params.get("login"));

        if (authDAO.getSnPass((String) params.get("login")).equals((String) params.get("pass"))) {
                sessionCreateDAO.createSession(userId,request, response);
                String additional = authDAO.findUserRel((String) params.get("login"));
                if (additional.equals("noteexists")){
                    return "/";
                }else{
                    String additional2 = authDAO.findAuthMain((String) params.get("login"));
                    if (additional2.substring(0,1).equals("2")) {
                        return "login.html#REQUESTPOPUP";
                    }else {
                    return "notifforuser.html";
                }
                }
        }else{
                return "injection_fixed";
        }
    }


    @RequestMapping(params = "send_email_activation", method = POST)
    public Map<String, String> resendActivationCode(HttpServletRequest request) throws CertificateException {
        MDC.put("action", "send email activation");
        X509Certificate cert = getRequestCert(request);
        if (cert == null) {
            logger.error("no user cert");
            throw new AccessDeniedException();
        }
        UserCertInfo userCertInfo = userCertInfoDAO.getUserCertInfo(cert);
        userService.sendEmailActivationMail(userCertInfo.getIin(), userCertInfo.getBin());
        return singletonMap("messages", messageDAO.get("EMAIL_ACTIVATION_CODE_SENT"));
    }

    @RequestMapping(params = "send_pass_reset_code", method = POST)
    public Map<String, String> resendActivationCode(@RequestParam("role_code") String role,
                                                    @CurrentUser User user,
                                                    HttpServletRequest request) throws CertificateException {
        MDC.put("action", "send pass reset code");
        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        logger.debug("role: {}", role);
        String email = userService.sendPasswordResetMail(role);
        return singletonMap("email", email);
    }

    @RequestMapping(params = "register_supplier", method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void registerSupplier(@RequestBody SupplierRegData supplierRegData,
                                 HttpServletRequest request) throws CertificateException, IOException {
        MDC.put("action", "register supplier");
        X509Certificate cert = getRequestCert(request);
        if (cert == null)
            throw new AccessDeniedException();
        UserCertInfo certInfo = userCertInfoDAO.findWithExistCheck(cert);
        supplierService.register(certInfo, supplierRegData.employee, supplierRegData.supplier);
    }

    @RequestMapping(params = "register_user", method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void registerUser(@RequestBody Employee emp,
                             HttpServletRequest request) throws CertificateException, IOException {
       // MDC.put("action", "register user");

        logger.debug("customer user exists: {}, supplier user exists: {}"+emp.getUserId());
  /*      UserCertInfo u = new UserCertInfo();
        u.setCountry("KZ");
        u.setBin("011140000493");
        // u.setIin("011140000493");
        u.setLastName("Alimovich");
        u.setFirstName("Alim");
        u.setOrgName("Alibi i co");
        u.setMiddleName("Alimovich");
        u.setEmail("orel.net@mail.ru");
        u.setIndividual(true);
        u.setSupplierExists(true);
        u.setCustomerExists(true);
        u.setCustomerUserExists(true);
        u.setSupplierUserExists(true);*/
       // employeeService.selfRegister(u, emp);
    }

    public X509Certificate getRequestCert(HttpServletRequest req) throws CertificateException {
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
        return null;
    }

    public X509Certificate getRequestCertFromHeader(HttpServletRequest req) throws CertificateException {
        String clientCert = req.getHeader("SSL_CLIENT_CERT");
        if (clientCert != null && !"".equals(clientCert)) {
            clientCert = clientCert.trim().replaceAll("\t", "\n").trim();
            InputStream is = new ByteArrayInputStream(clientCert.getBytes(StandardCharsets.UTF_8));
            CertificateFactory cf1 = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf1.generateCertificate(is);
        } else
            return null;
    }

    static class SupplierRegData {
        Supplier supplier;
        Employee employee;
    }
}
