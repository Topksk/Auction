package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class BidParticipationReportRequiredException extends ApplException {

    public BidParticipationReportRequiredException() {
        super("BID_PARTICIPATION_APPL_REQUIRED");
    }
}
