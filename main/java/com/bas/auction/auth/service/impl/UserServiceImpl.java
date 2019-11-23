package com.bas.auction.auth.service.impl;

import com.bas.auction.auth.dao.OneTimeCodeDAO;
import com.bas.auction.auth.dao.OneTimeCodeDAOEmail;
import com.bas.auction.auth.dao.UserDAO;
import com.bas.auction.auth.dto.OneTimeCode;
import com.bas.auction.auth.dto.OneTimeCodeForEmail;
import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.*;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.core.utils.validation.PasswordValidator.Strength;
import com.bas.auction.core.utils.validation.ShortPasswordException;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.core.utils.validation.WeakPasswordException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDAO userDAO;
    private final CryptoUtils cryptoUtils;
    private final OneTimeCodeDAO oneTimeCodeDAO;
    private final OneTimeCodeDAOEmail oneTimeCodeDAOEmail;
    private final UserNotificationService userNotifService;

    @Autowired
    public UserServiceImpl(UserDAO userDAO, CryptoUtils cryptoUtils, OneTimeCodeDAO oneTimeCodeDAO,
                           OneTimeCodeDAOEmail oneTimeCodeDAOEmail, UserNotificationService userNotifService) {
        this.userDAO = userDAO;
        this.cryptoUtils = cryptoUtils;
        this.oneTimeCodeDAO = oneTimeCodeDAO;
        this.oneTimeCodeDAOEmail = oneTimeCodeDAOEmail;
        this.userNotifService = userNotifService;
    }

    @Override
    public User findById(Long userId) {
        return userDAO.findById(userId);
    }

    @Override
    public List<User> findBySupplierId(Long supplierId) {
        return userDAO.findBySupplierId(supplierId);
    }

    @Override
    public String findPasswordHashByLogin(String login) {
        return userDAO.findPasswordHashByLogin(login);
    }

    @Override
    public Entry<Long, Boolean> findIdAndIsActiveByLogin(String login) {
        return userDAO.findIdAndIsActiveByLogin(login);
    }

    @Override
    public boolean findIsUserLoginExists(String login) {
        return userDAO.userLoginExists(login);
    }

    @Override
    public boolean customerUserExists(String iin, String bin) {
        String login = getCustomerLogin(iin, bin);
        return userDAO.userLoginExists(login);
    }

    @Override
    public boolean supplierUserExists(String iin, String bin) {
        String login = getResidentSupplierLogin(iin, bin);
        return userDAO.userLoginExists(login);
    }

    public List<Map<String, Object>> getEmployeeEmail(Long userId) {
        return userDAO.getSupplEmail(userId);
    }

    public List<Map<String, Object>> getEmployeeByRelId(Long relId) {
        return userDAO.getSupplEmailByRelId(relId);
    }

    public List<Map<String, Object>> getEmployeeEmail2(Long userId) {
        return userDAO.getSupplEmail2(userId);
    }

    public List<Map<String, Object>> getKskByUserId(Long userId) {
        return userDAO.getKskByUserId(userId);
    }

    public List<Map<String, Object>> getRelIdByUserParams(Map<String, Object> params){
        logger.info("getRelIdByUserParams {}");
        return userDAO.getRelIdByUserParams(params);
    }

    public List<Map<String, Object>> getKskByUserParams(Map<String, Object> params){
        logger.info("getKskByUserParams {}");
        return userDAO.getKskByUserParams(params);
    }

    public List<Map<String, Object>> getKskEmailAddress(Long userId, Object relId){
        logger.debug("getKskEmail {}" +relId);
        return userDAO.getKskEmail(userId, relId);

    }
    public String getSupplAddress(Long userId, Long relId) {
        logger.debug("getSupplAddress===={}");
        return userDAO.getSupplAddress( userId, relId);
    }


    @Override
    @SpringTransactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        logger.debug("change password: {}", userId);
        checkPassword(userId, oldPassword);
        validateNewPasswordStrength(newPassword);
        String newPasswordEncrypted = cryptoUtils.genStrongPasswdHash(newPassword);
        String email = userDAO.changePassword(userId, newPasswordEncrypted);
        if (email == null)
            return;
        userNotifService.sendPasswordChangedNotification(email);
    }

    @SpringTransactional
    public void newPassword(Long userId, String newPassword) {
        logger.debug("new password: {}", userId);
        validateNewPasswordStrength(newPassword);
        String newPasswordEncrypted = cryptoUtils.genStrongPasswdHash(newPassword);
        String email = userDAO.changePassword(userId, newPasswordEncrypted);
        if (email == null)
            return;
        userNotifService.sendPasswordChangedNotification(email);
    }

    @Override
    @SpringTransactional
    public long activateEmail(String code) {
        logger.debug("email activation using code: {}", code);
        String userEmail = findEmailActivationCodeUserEmail(code);
        long userId= userDAO.findUserIdByEmail(userEmail);
        userDAO.activateEmail(userEmail);
        return userId;
    }


    private String findEmailActivationCodeUserEmail(String code) {
        OneTimeCode otc = oneTimeCodeDAO.findOneTimeCode(code);
        logger.debug("findEmailActivationCodeUserEmail Email : {}"+otc.getEmail());
        if (otc == null)
            throw new EmailActivationCodeNotFoundException();
        oneTimeCodeDAOEmail.delete(code);
        if (new Date().compareTo(otc.getActiveTo()) > 0)
            throw new EmailActivationCodeExpiredException();
        return otc.getEmail();
    }


    private Long findEmailActivationCodeUserId(String code) {
        OneTimeCode otc = oneTimeCodeDAO.findOneTimeCode(code);
        if (otc == null)
            throw new EmailActivationCodeNotFoundException();
        oneTimeCodeDAO.delete(code);
        if (new Date().compareTo(otc.getActiveTo()) > 0)
            throw new EmailActivationCodeExpiredException();
        return otc.getUserId();
    }

    @Override
    @SpringTransactional
    public void resetPassword(String passwordResetCode, String newPassword) {
        logger.debug("passwd reset: {}", passwordResetCode);
        validateNewPasswordStrength(newPassword);
        String userEmail = findResetPasswordCodeUserId(passwordResetCode);
        if (!userEmail.equals("demo@topksk.kz")&&!userEmail.equals("akimat1@topksk.kz")) {
            String email = userDAO.resetPassword(userEmail, cryptoUtils.genStrongPasswdHash(newPassword));
            userNotifService.sendPasswordChangedNotification(email);
        }
    }

    private String findResetPasswordCodeUserId(String code) {
        OneTimeCode otc = oneTimeCodeDAO.findOneTimeCode(code);
        if (otc == null)
            throw new PasswordResetCodeNotFoundException();
        oneTimeCodeDAO.delete(code);
        if (new Date().compareTo(otc.getActiveTo()) > 0)
            throw new PasswordResetCodeExpiredException();
        return otc.getEmail();
    }

    @Override
    @SpringTransactional
    public void sendEmailActivationMail(String iin, String bin) {
        logger.debug("email activation: iin = {}, bin = {}", iin, bin);
        List<Map<String, Object>> infos = userDAO.findEmailNotActivatedUserInfo(iin, bin);
        for (Map<String, Object> info : infos) {
            String email = (String) info.get("email");
            if (email == null) {
                logger.error("no user email found for activation");
                continue;
            }
            String code = oneTimeCodeDAO.create((Long) info.get("user_id")).getCode();
            String fullName = (String) info.get("full_name");
            userNotifService.sendEmailActivationMail(email, code, fullName);
        }
    }

    @Override
    @SpringTransactional
    public String sendPasswordResetMail(String role) {
        logger.debug("reset passwd: role = {}", role);
        Map<String, Object> info;

        if (userDAO.findCustomerUserInfoForEmailNotif(role)==null){
            logger.error("no user email found for password reset");
            return "email_not_found";
        }

        info = userDAO.findCustomerUserInfoForEmailNotif(role);

        String email = (String) info.get("usermail1");
        if (email == null) {
            logger.error("no user email found for password reset");
            return null;
        }
        logger.error("hohohohoh");
        String fullName = (String) info.get("full_name");
        OneTimeCodeForEmail otc = oneTimeCodeDAOEmail.createCode((String) info.get("usermail1"));
        String code = otc.getCode();
        userNotifService.sendPasswordResetMail(email, code, fullName);
        return email;
    }

    @Override
    public void sendNewUserNotifToMainUser(String bin) {
        logger.debug("new user notif: {}", bin);
        List<String> emails = userDAO.findMainUserEmailsByIin(bin);
        if (emails.isEmpty()) {
            logger.error("no main user email found for new user notification");
            return;
        }
        userNotifService.sendNewUserNotifToMainUser(emails);
    }

    @Override
    public String getCustomerLogin(String iin, String bin) {
        return iin + "_" + bin + "_U";
    }

    @Override
    public String getResidentSupplierLogin(String iin, String bin) {
        return iin + "_" + bin + "_S";
    }

    @Override
    public String newSupplierUserLogin(String iin, String bin, boolean nonresident) {
        String prefix = iin;
        if (nonresident) {
            prefix = generateNonresidentUserLogin();
        }
        return prefix + "_" + bin + "_S";
    }

    private String generateNonresidentUserLogin() {
        Long noresid = userDAO.nonResidentSupplierUserSeqNextVal();
        return StringUtils.leftPad(noresid.toString(), 12, "0");
    }

    @Override
    @SpringTransactional
    public User create(User user, User data) {


        if (data.getPassword()!= null) {
            
        validateEmail(data.getEmail());
        validateNewPasswordStrength(data.getPassword());
        String password = cryptoUtils.genStrongPasswdHash(data.getPassword());
        data.setPassword(password);
        }
        data.setMainUser(data.isMainUser());
        /*String snPassword = cryptoUtils.genStrongPasswdHash(data.getFbpass());
        data.setFbpass(snPassword);*/
        logger.debug("parameterrrrrrrrrrrrr={}33333");
        if (data.isMainUser())
            userDAO.disableMainUserFlag(user, data);
        return userDAO.insert(user, data);
    }

    @Override
    @SpringTransactional
    public User update(User user, User data) {
        validateEmail(data.getEmail());
        int wasActive = userDAO.findIsActive(data.getUserId());
        userDAO.update(user, data);
        if (wasActive != 1) {
            OneTimeCodeForEmail otc =  oneTimeCodeDAOEmail.createCode( data.getEmail());
            String code = otc.getCode();
            String fullName = (data.getName()+" "+data.getSurname());
            userNotifService.sendEmailActivationMail(data.getEmail(), code, fullName);
        }
        return data;
    }

    @Override
    @SpringTransactional
    public User updatePersonal(User user, User data) {
        validateEmail(data.getEmail());
        return userDAO.updatePersonal(user, data);
    }

    private void validateNewPasswordStrength(String password) {
        Strength strength = Validator.isValidPassword(password);
        if (strength == Strength.SHORT) {
            logger.debug("short password");
            throw new ShortPasswordException();
        }
        if (strength == Strength.WEAK) {
            logger.debug("weak password");
            throw new WeakPasswordException();
        }
    }

    private void validateMainUserFlag(User data) {
        boolean isMainUser = userDAO.findIsMainUser(data.getUserId());
        if (isMainUser && !data.isMainUser())
            throw new NoMainUserException();
    }

    private void validateEmail(String email) {
        if (!Validator.isValidEmail(email))
            throw new EmailValidationException();
    }

    private void checkPassword(Long userId, String password) {
        String pwdHash = userDAO.findPasswordHashById(userId);
        boolean valid = cryptoUtils.checkPassword(password, pwdHash);
        if (!valid)
            throw new InvalidCredentialsException();
    }

    @Override
    public void checkPasswordByEmail(String email, String password) {
        String pwdHash = userDAO.findPasswordHashByEmail(email);
        boolean valid = cryptoUtils.checkPassword(password, pwdHash);
        if (!valid)
            throw new InvalidCredentialsException();
    }

    @Override
    public void sendUserRegNotif(String email, String password) {
        logger.debug("parameterrrrrrrrrrrrr={}12222");
        userNotifService.sendUserRegNotif(email, password);
    }

    public void sendUserRegNotifToKSK(String email, String address, String fio) {
        logger.debug("parameterrrrrrrrrrrrr={}123333");
        userNotifService.sendUserRegNotifToKsk( email, address, fio);
    }
    @Override
    public void sendCrReqMessUser(String userMail, Long reqId, String msg_code, String msg_lang) {
        //logger.debug("requestttt={}12222");
        userNotifService.sendCrReqMessUser(userMail, reqId, msg_code, msg_lang);
    }

    @Override
    public String sendNotif(Map<String, String> params) {
        return userNotifService.sendOtherMess(params);
    }
}
