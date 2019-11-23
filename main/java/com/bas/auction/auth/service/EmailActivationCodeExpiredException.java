package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class EmailActivationCodeExpiredException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3383536002332147696L;

	public EmailActivationCodeExpiredException() {
		super("EMAIL_ACTIVATION_CODE_EXPIRED");
	}
}
