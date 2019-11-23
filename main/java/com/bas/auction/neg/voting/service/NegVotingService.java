package com.bas.auction.neg.voting.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

public interface NegVotingService {
	Negotiation finishVoting(User user, Long negId);

	Negotiation resumeVoting(User user, Long negId);
}
