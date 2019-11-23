package com.bas.auction.bid.draft.service;

import com.bas.auction.core.ApplException;

public class OnlyDraftBidCanBeDeleted extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6245984230087438897L;

	public OnlyDraftBidCanBeDeleted() {
		super("ONLY_DRAFT_BID_CAN_BE_DELETED");
	}
}
