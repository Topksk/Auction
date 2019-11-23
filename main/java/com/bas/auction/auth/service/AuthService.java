package com.bas.auction.auth.service;

import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.auth.dto.UserCertInfo;

import java.util.List;
import java.util.Map;

public interface AuthService {
	UserAuthInfo findCustomerAuthInfo(String iin, String bin);

	UserAuthInfo findSupplierAuthInfo(String iin, String bin);

	String findAdminLogin(String email);

	Map<String, Object> findAuthInfo(String email_login);

	List<Map<String, Object>> findCityInfo(String city);

	List<Map<String, Object>> findStreetInfo(int id);

}
