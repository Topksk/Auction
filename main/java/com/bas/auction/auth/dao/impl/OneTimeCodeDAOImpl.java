package com.bas.auction.auth.dao.impl;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bas.auction.auth.dao.OneTimeCodeDAO;
import com.bas.auction.auth.dto.OneTimeCode;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;

@Repository
public class OneTimeCodeDAOImpl implements OneTimeCodeDAO, GenericDAO<OneTimeCode> {
	private final DaoJdbcUtil daoutil;
	private final CryptoUtils cryptoUtils;

	@Autowired
	public OneTimeCodeDAOImpl(DaoJdbcUtil daoutil, CryptoUtils cryptoUtils) {
		this.daoutil = daoutil;
		this.cryptoUtils = cryptoUtils;
	}

	@Override
	public String getSqlPath() {
		return "auth/onetime_code";
	}

	@Override
	public Class<OneTimeCode> getEntityType() {
		return OneTimeCode.class;
	}

	@Override
	public OneTimeCode findOneTimeCode(String code) {
		return daoutil.queryForObject(this, "get_onetime_code", code);
	}

	@Override
	public OneTimeCode create(Long userId) {
		OneTimeCode otc = new OneTimeCode();
		otc.setUserId(userId);
		otc.setCode(cryptoUtils.generateRandomString());
		return create(otc);
	}

	protected OneTimeCode create(OneTimeCode data) {
		if (data.getActiveFrom() == null) {
			data.setActiveFrom(new Date());
		}
		if (data.getActiveTo() == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(data.getActiveFrom());
			cal.add(Calendar.HOUR, 1);
			data.setActiveTo(cal.getTime());
		}
		Object[] values = { data.getUserId(), data.getCode(), data.getActiveFrom(), data.getActiveTo() };
		daoutil.insert(this, values);
		return data;
	}

	@Override
	public void delete(String code) {
		daoutil.delete(this, new Object[] { code });
	}
}
