package com.bas.auction.neg.unlock.service;

import com.bas.auction.neg.dto.Negotiation;

import java.util.List;

public interface NegUnlockService {
	List<Long> findUnlockList();

	Negotiation unlock(Long negId);

	void autoUnlock(Long negId);
}
