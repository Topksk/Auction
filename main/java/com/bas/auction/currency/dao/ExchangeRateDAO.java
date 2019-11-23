package com.bas.auction.currency.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.bas.auction.auth.dto.User;
import com.bas.auction.currency.dto.ExchangeRate;

public interface ExchangeRateDAO {
	Date findRateAvailableMaxDate();

	void insert(User user, List<ExchangeRate> rates);

	BigDecimal findCurrentExchangeRate(String currency);

	List<ExchangeRate> findExchangeRates(String currency, Date from, Date to);
}
