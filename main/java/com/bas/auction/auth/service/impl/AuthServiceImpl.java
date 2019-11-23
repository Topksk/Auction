package com.bas.auction.auth.service.impl;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.auth.dto.UserAuthInfo.RegStatus;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.auth.service.AuthService;
import com.bas.auction.core.dao.MessageDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {
    private final MessageDAO messages;
    private final AuthDAO authDAO;

    @Autowired
    public AuthServiceImpl(MessageDAO messages, AuthDAO authDAO) {
        this.messages = messages;
        this.authDAO = authDAO;
    }

    @Override
    public UserAuthInfo findCustomerAuthInfo(String iin, String bin) {
        UserAuthInfo authInfo = authDAO.findCustomerAuthInfo(iin, bin);
        if (authInfo == null)
            return null;
        if (authInfo.getLogin() == null) {
            authInfo.setRegStatusEnum(RegStatus.NOT_REGISTERED_USER);
        } else if (authInfo.getRegStatusEnum() == RegStatus.IN_PROGRESS) {
            authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.IN_PROGRESS_CUSTOMER);
        } else if (!authInfo.isActive()) {
            authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.NOT_ACTIVE_CUSTOMER_USER);
        } else if (authInfo.getRegStatusEnum() == RegStatus.AGREEMENT_NOT_SIGNED) {
            if (!authInfo.isMainUser())
                authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.AGREEMENT_NOT_SIGNED);
        }
        return authInfo;
    }

    @Override
    public UserAuthInfo findSupplierAuthInfo(String iin, String bin) {
        UserAuthInfo authInfo = authDAO.findSupplierAuthInfo(iin, bin);
        if (authInfo == null)
            return null;
        if (authInfo.getRegStatusEnum() == RegStatus.REJECTED) {
            if (!authInfo.isMainUser())
                authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.REJECTED_SUPPLIER);
        } else if (authInfo.getRegStatusEnum() == RegStatus.SENT_FOR_APPROVAL) {
            authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.NOT_APPROVED_SUPPLIER);
        } else if (authInfo.getRegStatusEnum() == RegStatus.IN_PROGRESS) {
            if (!authInfo.isMainUser())
                authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.IN_PROGRESS_SUPPLIER);
        } else if (authInfo.getLogin() == null) {
            authInfo.setRegStatusEnum(RegStatus.NOT_REGISTERED_USER);
        } else if (!authInfo.getEmailActivated()) {
            authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.EMAIL_NOT_ACTIVE_SUPPLIER_USER);
        } else if (!authInfo.isActive()) {
            authInfo.setLogin(null);
            authInfo.setRegStatusEnum(RegStatus.NOT_ACTIVE_SUPPLIER_USER);
        }
        return authInfo;
    }

    @Override
    public String findAdminLogin(String email) {
        return authDAO.findAdminLogin(email);
    }

    @Override
    public Map<String, Object> findAuthInfo(String email_login) {
        Map<String, Object> res = new HashMap<>();
        List<String> roles = new ArrayList<>();
        String login = findAdminLogin(email_login);
        boolean hasLogin = login != null;
        if (hasLogin)
            res.put("redirect_to", "login.html");
        if (!roles.isEmpty()) {
            res.put("roles", roles);
        }
        return res;
    }

    public List<Map<String, Object>> findCityInfo(String city){

        return authDAO.findAllCities();
    }


    public List<Map<String, Object>> findStreetInfo(int id){

        return authDAO.findAllStreet(id);
    }

    private String getStatusMessage(UserAuthInfo info, String bin) {
        if (info == null || info.getRegStatusEnum() == null)
            return null;
        RegStatus status = info.getRegStatusEnum();
        if (status == RegStatus.NOT_REGISTERED_USER) {
            return messages.get("NOT_REGISTERED_USER", Collections.singletonMap("BIN", bin));
        } else
            return messages.get(status.toString());
    }

    private Collection<String> getStatusMessages(UserAuthInfo custAuthInfo, UserAuthInfo suppAuthInfo, String bin) {
        Set<String> msgs = new HashSet<>();
        msgs.add(getStatusMessage(custAuthInfo, bin));
        msgs.add(getStatusMessage(suppAuthInfo, bin));
        msgs.remove(null);
        return msgs;
    }
}
