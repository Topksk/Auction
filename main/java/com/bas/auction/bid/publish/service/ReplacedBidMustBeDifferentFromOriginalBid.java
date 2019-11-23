package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class ReplacedBidMustBeDifferentFromOriginalBid extends ApplException {
    public ReplacedBidMustBeDifferentFromOriginalBid() {
        super("REPL_BID_MUST_BE_DIFFERENT");
    }
}
