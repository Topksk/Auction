package com.bas.auction.neg.award.service;

import com.bas.auction.core.ApplException;

public class NegVotingShouldBeFinishedException extends ApplException {
    public NegVotingShouldBeFinishedException() {
        super("NEG_VOTING_SHOULD_BE_FINISHED");
    }
}
