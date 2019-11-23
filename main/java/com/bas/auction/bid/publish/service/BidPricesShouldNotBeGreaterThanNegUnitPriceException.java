package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class BidPricesShouldNotBeGreaterThanNegUnitPriceException extends ApplException {
    public BidPricesShouldNotBeGreaterThanNegUnitPriceException() {
        super("BID_PRICES_SHOULD_NOT_BE_GREATER_THAN_NEG_UNIT_PRICE");
    }
}
