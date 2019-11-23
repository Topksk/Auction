package com.bas.auction.profile.bankaccount.service;

import com.bas.auction.core.ApplException;

public class BankAccountAlreadyExistsException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3941930250943809653L;

	public BankAccountAlreadyExistsException() {
		super("BANK_ACCOUNT_EXISTS");
	}
}
