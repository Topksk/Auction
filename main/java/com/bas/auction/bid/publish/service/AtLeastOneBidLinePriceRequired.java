package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class AtLeastOneBidLinePriceRequired extends ApplException {
    public AtLeastOneBidLinePriceRequired() {
        super("AT_LEAST_ONE_BID_LINE_PRICE_REQUIRED");
    }
}
