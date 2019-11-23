package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class PasswordResetCodeExpiredException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1549218773054637007L;

	public PasswordResetCodeExpiredException() {
		super("PASSWORD_RESET_CODE_EXPIRED");
	}
}
