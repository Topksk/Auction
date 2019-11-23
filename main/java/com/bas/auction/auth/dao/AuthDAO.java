package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.UserAuthInfo;

import java.util.List;
import java.util.Map;

public interface AuthDAO {
	UserAuthInfo findCustomerAuthInfo(String iin, String bin);

	UserAuthInfo findSupplierAuthInfo(String iin, String bin);

	String findAdminLogin(String email);

	String findAuthMain(String code);

	String findUserRel(String code);

	Long findUserIdByEmail(String code);

	String getSnPass(String code);

	Long findPosition(String email);

	List<Map<String, Object>> findAllCities();

	List<Map<String, Object>> findAllStreet(int id);

	List<Map<String, Object>> findOther(Map<String, Object> params);

}
