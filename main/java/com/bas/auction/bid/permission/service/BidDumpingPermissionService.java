package com.bas.auction.bid.permission.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

public interface BidDumpingPermissionService {
	void performNegBidsDumpingControl(User user, Negotiation neg);
}
