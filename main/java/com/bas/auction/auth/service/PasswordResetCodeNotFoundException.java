package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class PasswordResetCodeNotFoundException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8671429949690850335L;

	public PasswordResetCodeNotFoundException() {
		super("PASSWORD_RESET_CODE_NOT_FOUND");
	}
}
