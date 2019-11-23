package com.bas.auction.bid.service;

import com.bas.auction.core.ApplException;

public class BidSendFinishedException extends ApplException {

    public BidSendFinishedException() {
        super("BID_SEND_FINISHED");
    }
}
