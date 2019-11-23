package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class AtLeastOneParticipatedBidLineRequired extends ApplException {
    public AtLeastOneParticipatedBidLineRequired() {
        super("AT_LEAST_ONE_PARTICIPATED_BID_LINE_REQUIRED");
    }
}
