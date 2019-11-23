package com.bas.auction.auth.service.impl;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dao.SpravDAO;
import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.auth.dto.UserAuthInfo.RegStatus;
import com.bas.auction.auth.service.SpravService;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.request.dto.ReqSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

import java.util.*;

@Service
public class SpravServiceImpl implements SpravService {
    private final MessageDAO messages;
    private final AuthDAO authDAO;
    private final SpravDAO spravDAO;
    private final static Logger logger = LoggerFactory.getLogger(SpravService.class);

    @Autowired
    public SpravServiceImpl(MessageDAO messages, AuthDAO authDAO, SpravDAO spravDAO) {
        this.messages = messages;
        this.authDAO = authDAO;
        this.spravDAO = spravDAO;
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


    public List<Map<String, Object>> findSpravInfo(int lang_id, String name){
        return spravDAO.findAllSprav(lang_id, name);
    }

    public List<Map<String, Object>> findSpravInfoChild(int lang_id, String name, int sid){
        return spravDAO.findSpravChild(lang_id, name, sid);
    }

    public List<Map<String, Object>> findSpravChildById( String name, int sid){
        return spravDAO.findSpravChildById(name, sid);
    }
	public List<Map<String, Object>> findSpravChildByIdArr( String name,  int langid, String[] sid){
        return spravDAO.findSpravChildByIdArr(name, langid, sid);
    }
    @Override
    public List<Map<String, Object>> findReqs(ReqSearch params) {
        logger.debug("SpravService.findReqs.data = " + params.toString());
        return spravDAO.findReqs(params);
    }

    @Override
    public List<Map<String, Object>> findReqserv(ReqSearch params) {
        logger.debug("SpravService.findReqs.data = " + params.toString());
        return spravDAO.findReqserv(params);
    }

    @Override
    public List<Map<String, Object>> findCitReqs(ReqSearch params) {
        logger.debug("SpravService.findReqs.data = " + params.toString());
        return spravDAO.findCitReqs(params);
    }

    @Override
    public List<Map<String, Object>> findOther(Map<String, Object> params) {
        return spravDAO.findOther(params);
    }

    @Override
    public List<Map<String, Object>> findKskAddress(AddressSearch params) {
        logger.debug("SpravService.findKsk.data = " + params.toString());
        return spravDAO.findKskAddress(params);
    }

    @Override
    public List<Map<String, Object>> findAddressList(AddressSearch params) {
        logger.debug("SpravService.findAddressList.data = " + params.toString());
        return spravDAO.findAddressList(params);
    }

    @Override
    public List<Map<String, Object>> findKskEmpsList(KskEmpsSearch params) {
        logger.debug("SpravService.findKskEmpsList.data = " + params.toString());
        return spravDAO.findKskEmpsList(params);
    }

    @Override
    public List<Map<String, Object>> findServEmpsList(KskEmpsSearch params) {
        logger.debug("SpravService.findKskEmpsList.data = " + params.toString());
        return spravDAO.findServEmpsList(params);
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
