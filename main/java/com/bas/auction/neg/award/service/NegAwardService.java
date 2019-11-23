package com.bas.auction.neg.award.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

import java.util.List;
import java.util.Map.Entry;

public interface NegAwardService {
	void autoAward(Long negId, Long createdBy);

	List<Entry<Long, Long>> findAutoAwardList();

	Negotiation manualCloseNeg(User user, Long negId);

	void awardNeg(User user, Long negId);
}
