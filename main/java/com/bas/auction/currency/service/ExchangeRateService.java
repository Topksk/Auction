package com.bas.auction.currency.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.bas.auction.auth.dto.User;
import com.bas.auction.currency.dto.ExchangeRate;

public interface ExchangeRateService {
	Date findRateAvailableMaxDate();

	void create(User user, List<ExchangeRate> rates);

	BigDecimal findCurrentExchangeRate(String currency);

	List<ExchangeRate> findExchangeRates(String currency, Date from, Date to);
}
