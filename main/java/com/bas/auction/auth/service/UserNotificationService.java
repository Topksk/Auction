package com.bas.auction.auth.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.employee.dto.Employee;

import java.util.List;
import java.util.Map;

public interface UserNotificationService {

	void sendEmailActivationMail(String email, String code, String fullName);

	String sendPasswordResetMail(String email, String code, String fullName);

	void sendPasswordChangedNotification(String email);

	void sendNewUserNotifToMainUser(List<String> emails);

	void sendActivationNotif(User data);

	void sendUserRegNotif(String email, String password);
	
	void sendUserRegNotifToKsk(String email, String address, String fio);

	void sendSupplierMainUserEmailActivationMail(Employee emp, String code);

	void sendCrReqMessUser(String userMail, Long reqId, String msg_code, String msg_lang);

	void sendNotification(String[] emails, String subject, String content);
	
	String sendOtherMess(Map<String, String> params);

	String sendOtherMessToAll(Map<String, String> params);

	String sendPush(String email, String content);
}
