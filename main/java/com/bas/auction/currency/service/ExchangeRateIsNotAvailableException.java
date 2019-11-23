package com.bas.auction.currency.service;

import com.bas.auction.core.ApplException;

public class ExchangeRateIsNotAvailableException extends ApplException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6006012085473614941L;

	public ExchangeRateIsNotAvailableException() {
		super("NO_EXCHANGE_RATE");
	}
}
