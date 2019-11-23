package com.bas.auction.profile.employee.service.impl;


import com.bas.auction.auth.dao.OneTimeCodeDAO;
import com.bas.auction.auth.dao.OneTimeCodeDAOEmail;
import com.bas.auction.auth.dao.UserDAO;
import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserNotificationService;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.employee.dto.RegUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by alibi.balgabekov on 05.05.2016.
 */

@Service
public class UserRegService {

    private final Logger logger = LoggerFactory.getLogger(UserRegService.class);
    private final UserService userService;
    private final CryptoUtils cryptoUtils;
    private final UserNotificationService userNotifService;
    private final OneTimeCodeDAO oneTimeCodeDAO;
    private final OneTimeCodeDAOEmail oneTimeCodeDAOEmail;
    private final UserDAO userDAO;

@Autowired
    public UserRegService(UserService userService, CryptoUtils cryptoUtils, UserNotificationService userNotifService, OneTimeCodeDAO oneTimeCodeDAO, OneTimeCodeDAOEmail oneTimeCodeDAOEmail, UserDAO userDAO){

    this.userService=userService;
    this.cryptoUtils=cryptoUtils;
    this.userNotifService = userNotifService;
    this.oneTimeCodeDAO = oneTimeCodeDAO;
    this.oneTimeCodeDAOEmail = oneTimeCodeDAOEmail;
    this.userDAO = userDAO;
}


    public User getEmployeeUser(RegUser data) {
        User u = new User();
       // u.setActive(true);
        u.setEmail(data.getEmail());
        u.setPhoneNumber(data.getPhone());
        u.setMobilePhone(data.getMobilephone());
        u.setName(data.getName());
        u.setSurname(data.getSurname());
        u.setMidname(data.getMidname());
       // u.setMainUser(true);
        u.setPassword(data.getPassword());
       // u.setPersonId((long)212);
        u.setLogin(data.getEmail());
        u.setBirthday(data.getBirthdate());
        u.setIin(data.getIin());
        u.setFbpass(data.getFbpass());
        u.setSntrue(data.getSntrue());
        /*u.setCity(Integer.parseInt(data.getCity()));
        u.setStreet(Integer.parseInt(data.getStreet()));
        u.setHome(Integer.parseInt(data.getHome()));*/
       /* if(!data.getFraction().isEmpty()) {
            logger.debug("data.getFraction()="+data.getFraction());
            u.setFraction(data.getFraction());
        }else {
            u.setFraction("null");
        }
        u.setFlat(Integer.parseInt(data.getFlat()));
        if(!data.getFractflat().isEmpty()) {
            logger.debug("data.getFractflat()="+data.getFractflat());
            u.setFractflat(data.getFractflat());
        }else {
            u.setFractflat("null");
        }

        u.setRelation(data.getRelation());*/
       // u.setCustomerId(data.getCustomerId());
        //u.setSupplierId(data.getSupplierId());
        return u;
    }

    @SpringTransactional
    public User createCustomer(User user ,RegUser data) {
        User u = getEmployeeUser(data);

        String password = u.getPassword();
        List<Map<String, Object>> kskEmail;
        boolean genPasswd = password == null;
     /* for random generation
        if (genPasswd) {
            password = cryptoUtils.generateRandomString();
            u.setPassword(password);
        }*/

       User u1=userService.create(user, u);


        //Уведомление сотрудника КСК
       try {
            kskEmail=userService.getEmployeeEmail(u.getUserId());
           for (int i = 0; i<kskEmail.size(); i++){
           userService.sendUserRegNotifToKSK((String) kskEmail.get(i).get("usermail1"), userService.getSupplAddress(u.getUserId(), null), (data.getSurname() + " " + data.getName() + " " + ((data.getMidname() == null)?"":data.getMidname())));
           }
           if (!kskEmail.isEmpty()){
               userDAO.updateRelation(u.getUserId());
           }
       }catch (Exception e){

        }
       // userService.sendUserRegNotif(data.getEmail(), password);
    if(u.getSntrue()==null) {
        String code = oneTimeCodeDAOEmail.createCode(data.getEmail()).getCode();
    userNotifService.sendEmailActivationMail(data.getEmail(), code, u.getName());
    }
        return u1;
    }


    @SpringTransactional
    public User selfRegister (RegUser reguser){
        User sysadmin = User.sysadmin();
        return createCustomer(sysadmin, reguser);

    }



}
