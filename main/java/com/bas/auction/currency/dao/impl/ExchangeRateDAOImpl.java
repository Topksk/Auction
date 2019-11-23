package com.bas.auction.currency.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.currency.dao.ExchangeRateDAO;
import com.bas.auction.currency.dto.ExchangeRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Repository
public class ExchangeRateDAOImpl implements ExchangeRateDAO, GenericDAO<ExchangeRate> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public ExchangeRateDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "exchange_rate";
    }

    @Override
    public Class<ExchangeRate> getEntityType() {
        return ExchangeRate.class;
    }

    @Override
    public void insert(User user, List<ExchangeRate> rates) {
        List<Object[]> values = rates.stream()
                .map(rate -> mapToInsertValues(user, rate))
                .collect(toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] mapToInsertValues(User user, ExchangeRate rate) {
        return new Object[]{rate.getFromCurrency(), rate.getToCurrency(), rate.getActiveDate(), rate.getRate(),
                rate.getQuant(), user.getUserId(), user.getUserId()};
    }

    public List<ExchangeRate> findExchangeRates(String currency, Date from, Date to) {
        return daoutil.query(this, "get_exchange_rates", currency, from, to);
    }

    public Date findRateAvailableMaxDate() {
        return daoutil.queryScalar(this, "get_rate_max_date");
    }

    public BigDecimal findCurrentExchangeRate(String currency) {
        return daoutil.queryScalar(this, "get_rate", currency);
    }
}
