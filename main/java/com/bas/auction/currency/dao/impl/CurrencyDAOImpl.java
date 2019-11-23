package com.bas.auction.currency.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.currency.dao.CurrencyDAO;
import com.bas.auction.currency.dto.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CurrencyDAOImpl implements CurrencyDAO, GenericDAO<Currency> {
    private final static Logger logger = LoggerFactory.getLogger(CurrencyDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public CurrencyDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "currency";
    }

    @Override
    public Class<Currency> getEntityType() {
        return Currency.class;
    }

    @Override
    public List<Currency> findAll() {
        return daoutil.query(this, "currency_list");
    }

    @Override
    @SpringTransactional
    public void update(User user, List<Currency> data) {
        logger.trace("data: {}", data);
        List<Object[]> values = new ArrayList<>(data.size());
        for (Currency curr : data) {
            Object[] vals = {curr.isActive(), user.getUserId(), curr.getCode()};
            values.add(vals);
        }
        daoutil.batchUpdate(this, values);
    }
}
