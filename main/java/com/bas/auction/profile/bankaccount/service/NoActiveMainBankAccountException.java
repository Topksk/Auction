package com.bas.auction.profile.bankaccount.service;

import com.bas.auction.core.ApplException;

public class NoActiveMainBankAccountException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2440284226488106016L;

	public NoActiveMainBankAccountException() {
		super("MAIN_BANK_ACC_REQUIRED");
	}
}
