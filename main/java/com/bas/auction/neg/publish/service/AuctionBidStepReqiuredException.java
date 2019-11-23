package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class AuctionBidStepReqiuredException extends ApplException {
    public AuctionBidStepReqiuredException() {
        super("AUCTION_BID_STEP_REQUIRED");
    }
}
