package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.User;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface UserDAO {
	User findById(Long userId);

	List<User> findBySupplierId(Long supplierId);

	String findPasswordHashById(long userId);

	String findPasswordHashByEmail(String email);

	List<String> findMainUserEmailsByIin(String bin);

	Map<String, Object> findCustomerUserInfoForEmailNotif(String role);

	Map<String, Object> findSupplierUserInfoForEmailNotif(String iin, String bin);

	List<Map<String, Object>> findEmailNotActivatedUserInfo(String iin, String bin);

	long findUserIdByEmail(String login);

	String findPasswordHashByLogin(String login);

	boolean findIsMainUser(Long userId);

	Entry<Long, Boolean> findIdAndIsActiveByLogin(String login);

	int findIsActive(Long userId);

	Entry<String, String> findUserIinAndBin(Long userId);

	String changePassword(Long userId, String newPassword);

	void activateEmail(String userEmail);

	String resetPassword(String userEmail, String newPassword);

	User insert(User user, User data);

	void updateRelation(Long userId);

	User update(User user, User data);

	User updatePersonal(User user, User data);

	Long nonResidentSupplierUserSeqNextVal();

	void disableMainUserFlag(User user, User data);

	boolean userLoginExists(String login);

	List<Map<String, Object>> getSupplEmail(Long userId);

	List<Map<String, Object>> getSupplEmailByRelId(Long relId);

	List<Map<String, Object>> getSupplEmail2(Long userId);

	List<Map<String, Object>>  getKskByUserId(Long userId);

	List<Map<String, Object>> getRelIdByUserParams(Map<String, Object> params);

	List<Map<String, Object>> getKskByUserParams(Map<String, Object> params);

	List<Map<String, Object>> getKskEmail(Long userId, Object relId);

	String getSupplAddress(Long userId, Long relId);

	String getUserName(Long userId);

	void activateRelation(Long userId,  Object status, Object status1, Object rel_id, String sqlPath);
}
