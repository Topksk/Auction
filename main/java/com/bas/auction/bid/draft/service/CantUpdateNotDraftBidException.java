package com.bas.auction.bid.draft.service;

import com.bas.auction.core.ApplException;

public class CantUpdateNotDraftBidException extends ApplException {
    public CantUpdateNotDraftBidException() {
        super("CANT_UPDATE_NOT_DRAFT_BID");
    }
}
