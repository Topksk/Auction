package com.bas.auction.auth.dao;

import com.bas.auction.profile.request.dto.ReqSearch;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

public interface SpravDAO {

	List<Map<String, Object>> findAllSprav(int id, String name);

	List<Map<String, Object>> findSpravChild(int id, String name, int sid);

    List<Map<String, Object>> findSpravChildById(String name, int sid);

	List<Map<String, Object>> findSpravChildByIdArr(String name,  int langid, String[]  sid);

	List<Map<String, Object>> findReqs(ReqSearch params);

	List<Map<String, Object>> findReqserv(ReqSearch params);

	List<Map<String, Object>> findCitReqs(ReqSearch params);

	List<Map<String, Object>> findOther(Map<String, Object> params);

	List<Map<String, Object>> findKskAddress(AddressSearch params);
	List<Map<String, Object>> findAddressList(AddressSearch params);
	List<Map<String, Object>> findKskEmpsList(KskEmpsSearch params);
	List<Map<String, Object>> findServEmpsList(KskEmpsSearch params);

}
