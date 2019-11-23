package com.bas.auction.auth.dao.impl;

import java.util.Calendar;
import java.util.Date;

import com.bas.auction.auth.dto.OneTimeCodeForEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bas.auction.auth.dao.OneTimeCodeDAOEmail;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;

@Repository
public class OneTimeCodeDAOEmailImpl implements OneTimeCodeDAOEmail, GenericDAO<OneTimeCodeForEmail> {
    private final DaoJdbcUtil daoutil;
    private final CryptoUtils cryptoUtils;

    @Autowired
    public OneTimeCodeDAOEmailImpl(DaoJdbcUtil daoutil, CryptoUtils cryptoUtils) {
        this.daoutil = daoutil;
        this.cryptoUtils = cryptoUtils;
    }

    @Override
    public String getSqlPath() {
        return "auth/onetime_code_for_email";
    }

    @Override
    public Class<OneTimeCodeForEmail> getEntityType() {
        return OneTimeCodeForEmail.class;
    }


    @Override
    public OneTimeCodeForEmail findOneTimeCodeEmail(String code) {
        return daoutil.queryForObject(this, "get_onetime_code_email", code);
    }

		/*
	Copy of OneTimeCode, use for email init
	 */

    @Override
    public OneTimeCodeForEmail createCode(String email) {
        OneTimeCodeForEmail otc = new OneTimeCodeForEmail();
        otc.setUserEmail(email);
        otc.setCode(cryptoUtils.generateRandomString());
        return createForEmail(otc);
    }


    protected OneTimeCodeForEmail createForEmail(OneTimeCodeForEmail data) {
        if (data.getActiveFrom() == null) {
            data.setActiveFrom(new Date());
        }
        if (data.getActiveTo() == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(data.getActiveFrom());
            cal.add(Calendar.MONTH, 12);
            data.setActiveTo(cal.getTime());
        }
        Object[] values = {  null, data.getCode(), data.getActiveFrom(), data.getActiveTo(),data.getUserEmail(), };
        daoutil.insert(this, values);
        return data;
    }

    @Override
    public void delete(String code) {
        daoutil.delete(this, new Object[] { code });
    }
}
