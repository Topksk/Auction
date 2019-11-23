package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class InvalidCredentialsException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5568886576061944216L;

	public InvalidCredentialsException() {
		super("INVALID_CREDENTIALS");
	}
}
