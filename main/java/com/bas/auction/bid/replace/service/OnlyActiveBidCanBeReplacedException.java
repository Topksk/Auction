package com.bas.auction.bid.replace.service;

import com.bas.auction.core.ApplException;

public class OnlyActiveBidCanBeReplacedException extends ApplException{
    public OnlyActiveBidCanBeReplacedException() {
        super("ONLY_ACTIVE_BID_CANBE_REPLACED");
    }
}
