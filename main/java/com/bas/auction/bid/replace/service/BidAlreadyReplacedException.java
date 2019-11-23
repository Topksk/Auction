package com.bas.auction.bid.replace.service;

import com.bas.auction.core.ApplException;

public class BidAlreadyReplacedException extends ApplException {
    public BidAlreadyReplacedException() {
        super("BID_ALREADY_REPLACED");
    }
}
