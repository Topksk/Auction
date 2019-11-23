package com.bas.auction.neg.voting.service;

public interface NegVotingValidationService {
    void validateFinishVoting(Long negId);

    void validateResumeVoting(Long negId);
}
