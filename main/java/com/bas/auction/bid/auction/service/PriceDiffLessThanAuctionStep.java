package com.bas.auction.bid.auction.service;

import com.bas.auction.core.ApplException;

public class PriceDiffLessThanAuctionStep extends ApplException {
    public PriceDiffLessThanAuctionStep() {
        super("PRICE_DIFF_LESS_THAN_AUC_STEP");
    }
}
