package com.bas.auction.neg.voting.service;

import com.bas.auction.core.ApplException;

public class CantFinishVotingForNegNotInVotingStatusException extends ApplException{
    public CantFinishVotingForNegNotInVotingStatusException() {
        super("CANT_FINISH_VOTING_FOR_NEG_NOT_IN_VOTING_STATUS");
    }
}
