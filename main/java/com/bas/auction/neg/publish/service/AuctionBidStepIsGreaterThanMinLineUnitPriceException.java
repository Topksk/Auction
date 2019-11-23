package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class AuctionBidStepIsGreaterThanMinLineUnitPriceException extends ApplException {
    public AuctionBidStepIsGreaterThanMinLineUnitPriceException() {
        super("AUCTION_BID_STEP_MORE_THAN_PLAN_AMOUNT");
    }
}
