package com.bas.auction.bid.permission.service;

import com.bas.auction.core.ApplException;

public class CantUpdatePermissionForNegNotInVotingStatusException extends ApplException {
    public CantUpdatePermissionForNegNotInVotingStatusException() {
        super("CANT_UPDATE_PERMISSION_FOR_NEG_NOT_IN_VOTING_STATUS");
    }
}
