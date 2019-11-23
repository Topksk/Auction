package com.bas.auction.currency.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.Conf;
import com.bas.auction.currency.dao.ExchangeRateDAO;
import com.bas.auction.currency.dto.ExchangeRate;
import com.bas.auction.currency.service.ExchangeRateIsNotAvailableException;
import com.bas.auction.currency.service.ExchangeRateService;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
	private final static Logger logger = LoggerFactory.getLogger(ExchangeRateServiceImpl.class);
	private final ExchangeRateDAO exchangeRateDAO;
	private final Conf conf;

	@Autowired
	public ExchangeRateServiceImpl(ExchangeRateDAO exchangeRateDAO, Conf conf) {
		this.exchangeRateDAO = exchangeRateDAO;
		this.conf = conf;
	}

	@Override
	public Date findRateAvailableMaxDate() {
		Date maxDate = exchangeRateDAO.findRateAvailableMaxDate();
		if (maxDate == null) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			// last 90 days
			cal.add(Calendar.DATE, -90);
			maxDate = cal.getTime();
		}
		return maxDate;
	}

	@Override
	public void create(User user, List<ExchangeRate> rates) {
		exchangeRateDAO.insert(user, rates);
	}

	@Override
	public BigDecimal findCurrentExchangeRate(String currency) {
		logger.debug("get rate: {}", currency);
		String funcCurr = conf.getFunctionalCurrency();
		if (funcCurr == null)
			funcCurr = "KZT";
		if (funcCurr.equals(currency))
			return BigDecimal.ONE;
		BigDecimal rate = exchangeRateDAO.findCurrentExchangeRate(currency);
		logger.debug("rate: {}", rate);
		if (rate == null)
			throw new ExchangeRateIsNotAvailableException();
		return rate;
	}

	@Override
	public List<ExchangeRate> findExchangeRates(String currency, Date from, Date to) {
		logger.debug("get rates: currency = {}, from = {}, to = {}", currency, from, to);
		if (from == null)
			from = new Date(0);
		if (to == null)
			to = new Date(System.currentTimeMillis());
		return exchangeRateDAO.findExchangeRates(currency, from, to);
	}

}
