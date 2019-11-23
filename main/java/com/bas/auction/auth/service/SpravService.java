package com.bas.auction.auth.service;

import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.profile.request.dto.ReqSearch;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

public interface SpravService {
	UserAuthInfo findCustomerAuthInfo(String iin, String bin);

	UserAuthInfo findSupplierAuthInfo(String iin, String bin);

	String findAdminLogin(String email);

	Map<String, Object> findAuthInfo(String email_login);

	List<Map<String, Object>> findSpravInfo(int lang_id, String name);

	List<Map<String, Object>> findSpravInfoChild(int lang_id, String name, int sid);

    List<Map<String, Object>> findSpravChildById( String name, int sid);
	List<Map<String, Object>> findSpravChildByIdArr( String name, int langid,  String[] sid);
	List<Map<String, Object>> findReqs(ReqSearch params);
	List<Map<String, Object>> findReqserv(ReqSearch params);
	List<Map<String, Object>> findCitReqs(ReqSearch params);
	List<Map<String, Object>> findOther(Map<String, Object> params);

	List<Map<String, Object>> findKskAddress(AddressSearch params);
	List<Map<String, Object>> findAddressList(AddressSearch params);
	List<Map<String, Object>> findKskEmpsList(KskEmpsSearch params);
	List<Map<String, Object>> findServEmpsList(KskEmpsSearch params);

}
