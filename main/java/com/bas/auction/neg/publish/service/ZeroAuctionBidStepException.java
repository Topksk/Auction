package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class ZeroAuctionBidStepException extends ApplException {

    public ZeroAuctionBidStepException() {
        super("AUCTION_BID_STEP_ZERO");
    }
}
