package com.bas.auction.auth.dao.impl;

import com.bas.auction.profile.request.dto.ReqSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bas.auction.auth.dao.SpravDAO;
import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

@Repository
public class SpravDAOImpl implements SpravDAO, GenericDAO<UserAuthInfo> {
	private final DaoJdbcUtil daoutil;
    private final static Logger logger = LoggerFactory.getLogger(SpravDAO.class);

	@Autowired
	public SpravDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
	}

	@Override
	public String getSqlPath() {
		return "sprav";
	}

	@Override
	public Class<UserAuthInfo> getEntityType() {
		return UserAuthInfo.class;
	}

	public List<Map<String, Object>> findAllSprav(int id, String name) {
		//logger.debug(" findAllSprav, name = "+ name + ", id=" + id);
		return daoutil.queryForMapList(this, name, id);
        //return daoutil.queryForMapList(this, 'req_types', id);
	}

    @Override
    public List<Map<String, Object>> findSpravChild(int id, String name, int sid) {
        return daoutil.queryForMapList(this, name, id, sid);
    }

	@Override
	public List<Map<String, Object>> findSpravChildById( String name, int sid) {
		return daoutil.queryForMapList(this, name, sid);
	}	
	

	public List<Map<String, Object>> findSpravChildByIdArr(String name,  int langid, String[]  sid) {
		return daoutil.queryForMapListArray(this, name, langid,  sid);
	}
	
	@Override
	public List<Map<String, Object>> findReqs(ReqSearch params) {
		return daoutil.queryForMapList(this, "reqs_list", params);
	}

	@Override
	public List<Map<String, Object>> findReqserv(ReqSearch params) {
		return daoutil.queryForMapListServ(this, "reqserv_list", params);
	}

	@Override
	public List<Map<String, Object>> findCitReqs(ReqSearch params) {
		return daoutil.queryForMapList(this, "citreqs_list", params);
	}
	
	@Override
	public List<Map<String, Object>> findOther(Map<String, Object> params) {
		return daoutil.queryForMapList(params);
	}

	@Override
	public List<Map<String, Object>> findKskAddress(AddressSearch params) {
		return daoutil.queryForMapList(this, "addr_list", params);
	}

	@Override
	public List<Map<String, Object>> findAddressList(AddressSearch params) {
		return daoutil.queryForMapList(this, "all_address_list", params);
	}

	@Override
	public List<Map<String, Object>> findKskEmpsList(KskEmpsSearch params) {
		return daoutil.queryForMapList(this, "ksk_emps_list", params);
	}

	@Override
	public List<Map<String, Object>> findServEmpsList(KskEmpsSearch params) {
		return daoutil.queryForMapList(this, "serv_emps_list", params);
	}
}
