package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class ShortAuctionDurationException extends ApplException {

    public ShortAuctionDurationException(int auctionDuration) {
        super("SHORT_AUC_DURATION");
        List<Map<String, String>> params = singletonList(singletonMap("auction_duration", String.valueOf(auctionDuration)));
        setParams(params);
    }
}
