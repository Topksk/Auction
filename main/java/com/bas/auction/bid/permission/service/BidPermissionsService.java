package com.bas.auction.bid.permission.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.neg.dto.Negotiation;

import java.util.List;

public interface BidPermissionsService {
	void update(User user, List<BidLinePermissions> bidLinePermissions);

	void permitNegBidsInFunctionalCurrency(Long negId, Long requirementId);

	void createNegBidPermissions(Negotiation neg, Boolean defaultPermission);

	void createNegBidPermissions(Negotiation neg);
}
