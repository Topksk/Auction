package com.bas.auction.bid.draft.service;

import com.bas.auction.core.ApplException;

public class NoPermittedTender2Stage1BidException extends ApplException {
    public NoPermittedTender2Stage1BidException() {
        super("NO_PERMITTED_TENDER2_STAGE1_BID");
    }
}
