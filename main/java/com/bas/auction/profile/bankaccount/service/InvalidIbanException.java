package com.bas.auction.profile.bankaccount.service;

import com.bas.auction.core.ApplException;

public class InvalidIbanException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2154803436675292792L;

	public InvalidIbanException() {
		super("INVALID_IBAN");
	}
}
