package com.bas.auction.bid.permission.service;

import com.bas.auction.auth.dto.User;

public interface BidForeignCurrencyPermissionService {
	void performNegBidsForeignCurrencyControl(User user, Long negId);
}
