package com.bas.auction.currency.dao;

import java.util.List;

import com.bas.auction.auth.dto.User;
import com.bas.auction.currency.dto.Currency;

public interface CurrencyDAO {
	void update(User user, List<Currency> data);

	List<Currency> findAll();
}
