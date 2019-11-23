package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class AuctionBidStepPercentNotInRangeException extends ApplException {
    public AuctionBidStepPercentNotInRangeException() {
        super("AUCTION_BID_STEP_PERCENT_NOT_IN_RANGE");
    }
}
