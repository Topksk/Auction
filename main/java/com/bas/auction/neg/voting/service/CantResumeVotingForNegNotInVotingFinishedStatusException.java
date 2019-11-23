package com.bas.auction.neg.voting.service;

import com.bas.auction.core.ApplException;

public class CantResumeVotingForNegNotInVotingFinishedStatusException extends ApplException {
    public CantResumeVotingForNegNotInVotingFinishedStatusException() {
        super("CANT_RESUME_VOTING_FOR_NEG_NOT_IN_VOTING_FINISHED_STATUS");
    }
}
