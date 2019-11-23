package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class BidReportRequiredException extends ApplException {

    public BidReportRequiredException() {
        super("BID_REPORT_REQUIRED");
    }
}
