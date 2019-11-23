package com.bas.auction.auth.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserNotificationService;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.profile.employee.dto.Employee;
import org.elasticsearch.common.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.mail.MessagingException;
import java.util.*;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;


@Service
public class UserNotificationServiceImpl implements UserNotificationService {
    private final Logger logger = LoggerFactory.getLogger(UserNotificationServiceImpl.class);
    private final MessageDAO messages;
    private final Conf conf;
    private final MailService mailService;

    @Autowired
    public UserNotificationServiceImpl(Conf conf, MessageDAO messages, MailService mailService) {
        this.conf = conf;
        this.messages = messages;
        this.mailService = mailService;
    }

    private void sendMail(List<String> emails, String subjectCode, String bodyCode, Map<String, String> params) {
        String subject = messages.getFromDb(subjectCode, "RU");
        String body;
        if (params == null)
            body = messages.getFromDb(bodyCode, "RU");
        else
            body = messages.getFromDb(bodyCode, "RU", params);

        for (String email : emails) {
            try {
                mailService.sendMail(email, subject, body);
            } catch (MessagingException e) {
                logger.error("Error sending user notification: subjectCode={}", subjectCode, e);
            }
        }
    }

    private void sendMail(String email, String subjectCode, String bodyCode, Map<String, String> params) {
        sendMail(Collections.singletonList(email), subjectCode, bodyCode, params);
    }

    private void sendMail(String email, String subjectCode, String bodyCode) {
        sendMail(email, subjectCode, bodyCode, null);
    }

    @Override
    public void sendEmailActivationMail(String email, String code, String fullName) {
        String instUrl = conf.getInstructionsUrl();
        String actUrl = conf.getLoginHost() + "/activation.html?activate_email&code=" + code;
        Map<String, String> params = new HashMap<>();
        logger.debug("fullName{}{}"+fullName);
        params.put("RECIPIENT_NAME", fullName);
        params.put("ACTIVATION_URL", actUrl);
        params.put("INSTRUCTIONS_URL", instUrl);
        logger.debug("email activation info: code = {}, email = {}", code, email);
        sendMail(email, "EMAIL_ACTIVATION_SUBJECT", "EMAIL_ACTIVATION_BODY", params);
    }

    @Override
    public String sendPasswordResetMail(String email, String code, String fullName) {
        String url = conf.getLoginHost() + "/reset_password.html?code=" + code;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        Map<String, String> params = new HashMap<>();
        params.put("RECIPIENT_NAME", fullName);
        params.put("RESET_PASSWORD_URL", url);
        params.put("GENERATE_DATE",dateFormat.format(cal.getTime()) ); //2016/05/18 15:16:33
        cal.add(Calendar.HOUR, 1);
        params.put("LINK_DATE_EXPIRE",dateFormat.format(cal.getTime()) ); //2016/05/18 15:16:33
        logger.debug("password reset info: code = {}, email = {}", code, email);
        sendMail(email, "PASSWORD_RESET_SUBJECT", "PASSWORD_RESET_BODY", params);
        return email;
    }

    @Override
    public void sendNewUserNotifToMainUser(List<String> emails) {
        logger.debug("main user email for new user notif: {}", emails);
        sendMail(emails, "NEW_USER_REG_NOTIF_TO_MAIN_USER_SUBJECT", "NEW_USER_REG_NOTIF_TO_MAIN_USER_BODY", null);
    }

    @Override
    public void sendActivationNotif(User data) {
        String content, subject;
        if (data.isActive()) {
            logger.debug("user activated notif: id = {}, email = {}", data.getUserId(), data.getEmail());
            content = "USER_ACCOUNT_ACTIVATED_BODY";
            subject = "USER_ACCOUNT_ACTIVATED_SUBJECT";
        } else {
            logger.debug("user deactivated notif: id = {}, email = {}", data.getUserId(), data.getEmail());
            content = "USER_ACCOUNT_DEACTIVATED_BODY";
            subject = "USER_ACCOUNT_DEACTIVATED_SUBJECT";
        }
        sendMail(data.getEmail(), subject, content);
    }

    @Override
    public void sendPasswordChangedNotification(String email) {
        logger.debug("send passwd reset email: {}", email);
        sendMail(email, "PASSWORD_CHANGED_SUBJECT", "PASSWORD_CHANGED_BODY");
    }

    @Override
    public void sendUserRegNotif(String email, String password) {
        logger.debug("send user registered notif: {}", email);
        if (email == null) {
            logger.error("no email for user reg notif");
            return;
        }
        String loginUrl = conf.getLoginUrl();
        String instUrl = conf.getInstructionsUrl();
        Map<String, String> params = new HashMap<>();
        params.put("PASSWORD", password);
        params.put("LOGIN_URL", loginUrl);
        params.put("INSTRUCTIONS_URL", instUrl);
        String content = messages.getFromDb("USER_REG_BODY", "RU", params);
        String subject = messages.getFromDb("USER_REG_SUBJECT", "RU");
        try {
            mailService.sendMail(email, subject, content);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendUserRegNotifToKsk(String email, String address, String fio) {
        logger.debug("send user registered notif to KSK: {}", email);
        String url = conf.getLoginHost() + "/kskProfile.html#kskProfile?inquiry";// + code;
        if (email == null) {
            logger.error("no email for ksk notif");
            return;
        }
        String loginUrl = conf.getLoginUrl();
        String instUrl = conf.getInstructionsUrl();
        Map<String, String> params = new HashMap<>();
        params.put("REG_ADDRESS", address);
        params.put("REG_FIO", fio);
        params.put("ACCEPT_KSK", url);
        String content = messages.getFromDb("NEW_USER_REG_NOTIFICATION_TO_KSK", "RU", params);
        String subject = messages.getFromDb("USER_REG_SUBJECT", "RU");
        try {
            mailService.sendMail(email, subject, content);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendSupplierMainUserEmailActivationMail(Employee emp, String code) {
        logger.debug("sending email activation: userId = {}, email = {}", emp.getUserId(), emp.getEmail());
        String actUrl = conf.getHost() + "/activation.html?activate_email&code=" + code;
        String instUrl = conf.getInstructionsUrl();
        Map<String, String> params = new HashMap<>();
        params.put("RECIPIENT_NAME", emp.getFullName());
        params.put("ACTIVATION_URL", actUrl);
        params.put("INSTRUCTIONS_URL", instUrl);
        String bodyCode = "SUPPLIER_REG_EMAIL_ACTIVATION_BODY";
        String subjectCode = "SUPPLIER_REG_EMAIL_ACTIVATION_SUBJECT";
        sendMail(emp.getEmail(), subjectCode, bodyCode, params);
    }

    @Override
    public void sendCrReqMessUser(String userMail, Long reqId, String msg_code, String msg_lang) {
        String reqIdStr = String.valueOf(reqId);

        Map<String, String> params = new HashMap<>();
        params.put("REQ_ID", reqIdStr);

        String content = messages.getFromDb(msg_code + "_BODY", msg_lang, params);
        String subject = messages.getFromDb(msg_code + "_SUBJECT", msg_lang);
        try {
            mailService.sendMail(userMail, subject, content);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void sendNotification(String[] emails, String subject, String content) {
        String msg_code="PUSH_CIT_NOTIFICATION_BODY";
        Map<String, String> params = null;
        String contentPush = messages.getFromDb(msg_code, "RU", params);
        logger.info("contentPush="+contentPush);
        try {
            for (String email : emails) {
                logger.debug("sendNotification{}{}"+email);
                mailService.sendMail(email, subject, content);
                sendPush(email, contentPush);
            }
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }
	
    @Override
    public String sendOtherMess(Map<String, String> params) {
        String ret_val;
        logger.info("sendOtherMess");
        try {
            String msg_code=params.get("MSG_CODE");
            String msg_lang=params.get("MSG_LANG");
            String push_send=params.get("PUSH");
            String email=params.get("EMAIL");
            logger.info("code body="+msg_code + "_BODY" + ", lang="+msg_lang);
            String content = messages.getFromDb(msg_code + "_BODY", msg_lang, params);
            if (content==null)
                logger.error("content=null");
            else
                logger.info("content="+content);
            String subject = messages.getFromDb(msg_code + "_SUBJECT", msg_lang);
            if (subject==null) {
                logger.error("subject=null");
                throw null;
            }
            else {
                logger.info("subject="+subject);
                mailService.sendMail(email, subject, content);
                if (push_send != null) {
                    if (push_send.equals("1")){
                        msg_code="PUSH_" + msg_code;
                        logger.info("2.code body="+msg_code + "_BODY" + ", lang="+msg_lang);
                        content = messages.getFromDb(msg_code + "_BODY", msg_lang, params);
                        if (content==null)
                            logger.error("2.content=null");
                        else
                            logger.info("2.content="+content);
                        sendPush(email, content);
                    }
                }
                ret_val="OK";
            }
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            throw null;
        }
        return ret_val;
    }

    @Override
    public String sendOtherMessToAll(Map<String, String> params) {
        MDC.put("action", "sendOtherMessToAll");
        String ret_val;
        logger.info("sendOtherMessToAll");

        try {
            String msg_code=params.get("MSG_CODE");
            String msg_lang=params.get("MSG_LANG");
            String push_send=params.get("PUSH");

            logger.info("code body="+msg_code + "_BODY" + ", lang="+msg_lang);
            String content = messages.getFromDb(msg_code + "_BODY", msg_lang, params);
            if (content==null)
                logger.error("content=null");
            else
                logger.info("content="+content);
            String subject = messages.getFromDb(msg_code + "_SUBJECT", msg_lang);
            if (subject==null) {
                logger.error("line 284, not found [" + msg_code + "_SUBJECT] from auction.messages");
                throw null;
            }
            else {
                logger.info("subject="+subject);
                logger.info("params.EMAIL="+params.get("EMAIL"));
                String[] emails = params.get("EMAIL").replaceAll("^[,\\s]+", "").split("[,\\s]+");
                logger.info("KSK ds emails.length="+emails.length);
                if (emails[0].isEmpty()){
                    logger.info("emails.isEmpty");
                    emails = new String[2];
                    emails[0]="sales@cloudmaker.kz";
                    emails[1]="dev@cloudmaker.kz";
                    subject="[NOT EMAILS] "+subject;
                }
                else {
                    logger.info("emails[0]=["+emails[0]+"]");
                }
                mailService.sendMail(emails, subject, content);
                if (push_send != null) {
                    if (push_send.equals("1")){
                        msg_code="PUSH_" + msg_code;
                        logger.info("2.code body="+msg_code + "_BODY" + ", lang="+msg_lang);
                        content = messages.getFromDb(msg_code + "_BODY", msg_lang, params);
                        if (content==null)
                            logger.error("2.content=null");
                        else
                            logger.info("2.content="+content);
                        //sendPush(emails, content);
                        for (int i =0; i<emails.length; i++){
                            logger.info(" email ("+ i + ")=" + emails[i]);
                            sendPush(emails[i], content);
                        }
                    }
                }
                ret_val="OK";
            }
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
            throw null;
        }
        return ret_val;
    }

    public String sendPush(String email, String content) {
        int n_cp=290;
        String s_err;
        String strJsonBody;
        Date dCur;
        s_err="Ok";
        dCur=new Date();
        content=content + ", " + dCur.toLocaleString();
        email=email.toLowerCase();

        try {
            n_cp=296;
            String jsonResponse;
            String PushRest=conf.get("push.RestApi");
            String PushApid=conf.get("push.AppId");

            URL url = new URL("https://onesignal.com/api/v1/notifications");
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Authorization", "Basic " + PushRest);
            con.setRequestMethod("POST");

            n_cp=308;
            logger.info("email =" + email);
            logger.info("email.length()=" + email.length());

            if (email.length()>5) {
                strJsonBody = "{"
                        +   "\"app_id\": \"" + PushApid + "\","
                        +   "\"filters\": [{\"field\": \"tag\", \"key\": \"email\", \"relation\": \"=\", \"value\": \"" + email + "\"}],"
                        +   "\"data\": {\"foo\": \"bar\"},"
                        +   "\"contents\": {\"en\": \"" + content + "\"}"
                        + "}";

            }
            else {
                return "emails absent";
                /*strJsonBody = "{"
                        +   "\"app_id\": \"" + PushApid + "\","
                        +   "\"included_segments\": [\"All\"],"
                        +   "\"data\": {\"foo\": \"bar\"},"
                        +   "\"contents\": {\"en\": \"" + content + "\"}"
                        + "}";*/
            }

            byte[] sendBytes = strJsonBody.getBytes("UTF-8");
            con.setFixedLengthStreamingMode(sendBytes.length);

            OutputStream outputStream = con.getOutputStream();

            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            //System.out.println("httpResponse: " + httpResponse);
            logger.info("httpResponse: " + httpResponse);

            n_cp=337;
            if (  httpResponse >= HttpsURLConnection.HTTP_OK
                    && httpResponse < HttpsURLConnection.HTTP_BAD_REQUEST) {
                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            else {
                Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
            }
            n_cp=358;
            //System.out.println("jsonResponse:\n" + jsonResponse);
            logger.info("jsonResponse:\n" + jsonResponse);
        }
        catch(Throwable t) {
            t.printStackTrace();
            logger.info("sendpush, line: " + n_cp);
        }
        return s_err;
    }
}