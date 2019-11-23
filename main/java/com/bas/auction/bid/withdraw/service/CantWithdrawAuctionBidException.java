package com.bas.auction.bid.withdraw.service;

import com.bas.auction.core.ApplException;

public class CantWithdrawAuctionBidException extends ApplException {

    public CantWithdrawAuctionBidException() {
        super("CANT_WITHDRAW_AUC");
    }
}
