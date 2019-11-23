package com.bas.auction.profile.address.service;

import com.bas.auction.core.ApplException;

public class NoLegalAddressException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6577453926481556932L;

	public NoLegalAddressException() {
		super("LEGAL_ADDRESS_REQUIRED");
	}
}
