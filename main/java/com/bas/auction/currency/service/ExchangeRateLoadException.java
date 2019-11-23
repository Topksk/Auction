package com.bas.auction.currency.service;

public class ExchangeRateLoadException extends RuntimeException {

    public ExchangeRateLoadException() {

    }

    public ExchangeRateLoadException(String message) {
        super(message);
    }
}
