package com.bas.auction.neg.publish.service;


import com.bas.auction.core.ApplException;

public class NegativeAuctionBidStepException extends ApplException {
    public NegativeAuctionBidStepException() {
        super("AUCTION_BID_STEP_NEGATIVE");
    }
}
