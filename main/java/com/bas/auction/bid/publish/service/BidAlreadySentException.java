package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class BidAlreadySentException extends ApplException {
    public BidAlreadySentException() {
        super("BID_ALREADY_SENT_EXCEPTION");
    }
}
