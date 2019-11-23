package com.bas.auction.auth.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dto.UserAuthInfo;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;

import java.util.List;
import java.util.Map;

@Repository
public class AuthDAOImpl implements AuthDAO, GenericDAO<UserAuthInfo> {
	private final DaoJdbcUtil daoutil;

	@Autowired
	public AuthDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
	}

	@Override
	public String getSqlPath() {
		return "auth";
	}

	@Override
	public Class<UserAuthInfo> getEntityType() {
		return UserAuthInfo.class;
	}

	@Override
	public UserAuthInfo findCustomerAuthInfo(String iin, String bin) {
		return daoutil.queryForObject(this, "customer_user_info", iin, bin);
	}

	@Override
	public UserAuthInfo findSupplierAuthInfo(String iin, String bin) {
		return daoutil.queryForObject(this, "supplier_user_info", iin, bin);
	}

	@Override
	public String findAdminLogin(String email) {
		return daoutil.queryScalar(this, "admin_login", email);
	}

	public String findAuthMain(String code) {
		return daoutil.queryScalar(this, "is_main", code);
	}

	public String findUserRel(String code) {
		return daoutil.queryScalar(this, "get_relation_count", code);
	}

	public Long findUserIdByEmail(String code) {
		return daoutil.queryScalar(this, "get_user_id_by_email", code);
	}

	public String getSnPass(String code) {
		return daoutil.queryScalar(this, "getSnPass", code);
	}


	public Long findPosition(String email) {
		return daoutil.queryScalar(this, "cur_pos_type", email);
	}

	public List<Map<String, Object>> findAllCities() {

		return daoutil.queryForMapList(this, "all_city");
	}

	public List<Map<String, Object>> findAllStreet(int id) {

		return daoutil.queryForMapList(this, "all_street", id );
	}

	public List<Map<String, Object>> findOther(Map<String, Object> params) {
		return daoutil.queryForMapList(params);
}

}
