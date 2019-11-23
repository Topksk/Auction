package com.bas.auction.neg.draft.service;

import com.bas.auction.core.ApplException;

public class OnlyDraftNegCanBeDeletedException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1656228869710768780L;

	public OnlyDraftNegCanBeDeletedException() {
		super("ONLY_DRAFT_NEG_CAN_BE_DELETED");
	}
}
