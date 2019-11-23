package com.bas.auction.bid.discount.service;

import com.bas.auction.core.ApplException;

public class CantUpdateDiscountForNegNotInVotingStatusException extends ApplException {
    public CantUpdateDiscountForNegNotInVotingStatusException() {
        super("CANT_UPDATE_DISCOUNT_FOR_NEG_NOT_IN_VOTING_STATUS");
    }
}
