package com.bas.auction.bid.publish.service;

import com.bas.auction.core.ApplException;

public class CantChangeBidLineParticipationForAuction extends ApplException {
    public CantChangeBidLineParticipationForAuction() {
        super("CANT_CHANGE_BID_LINE_PARTICIPATION_FOR_AUCTION");
    }
}
