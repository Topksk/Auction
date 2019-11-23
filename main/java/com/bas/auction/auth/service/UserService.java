package com.bas.auction.auth.service;

import com.bas.auction.auth.dto.User;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface UserService {
	User findById(Long userId);

	List<User> findBySupplierId(Long supplierId);

	String findPasswordHashByLogin(String login);

	Entry<Long, Boolean> findIdAndIsActiveByLogin(String login);

	boolean findIsUserLoginExists(String login);

	void changePassword(Long userId, String oldPassword, String newPassword);

	void newPassword(Long userId, String newPassword);

	long activateEmail(String code);

	void resetPassword(String passwordResetcode, String newPassword);

	void sendEmailActivationMail(String iin, String bin);

	String sendPasswordResetMail(String role);

	void sendNewUserNotifToMainUser(String bin);

	boolean customerUserExists(String iin, String bin);

	boolean supplierUserExists(String iin, String bin);

	String getCustomerLogin(String iin, String bin);

	String getResidentSupplierLogin(String iin, String bin);

	List<Map<String, Object>> getEmployeeEmail(Long userId);

	List<Map<String, Object>> getEmployeeByRelId(Long relId);

	List<Map<String, Object>> getEmployeeEmail2(Long userId);

	List<Map<String, Object>> getKskByUserId(Long userId);

	List<Map<String, Object>> getKskByUserParams(Map<String, Object> params);

	List<Map<String, Object>> getRelIdByUserParams(Map<String, Object> params);

	List<Map<String, Object>> getKskEmailAddress(Long userId, Object relId);

	String getSupplAddress(Long userId, Long relId);

	String newSupplierUserLogin(String iin, String bin, boolean nonresident);

	User create(User user, User data);

	User update(User user, User data);

	User updatePersonal(User user, User data);

	void sendUserRegNotif(String email, String password);

	void sendUserRegNotifToKSK(String email, String address, String fio);
	
	void sendCrReqMessUser(String userMail, Long reqId, String msg_code, String msg_lang);

	String sendNotif(Map<String, String> params);

	void checkPasswordByEmail(String email, String password) ;
}
