package com.bas.auction.bid.draft.service;

import com.bas.auction.core.ApplException;

public class ActiveBidAlreadyExistsException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -298524065894713559L;

	public ActiveBidAlreadyExistsException() {
		super("ACTIVE_BID_EXISTS");
	}
}
