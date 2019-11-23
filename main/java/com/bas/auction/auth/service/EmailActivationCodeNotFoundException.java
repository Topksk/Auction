package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class EmailActivationCodeNotFoundException extends ApplException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2686196474525586910L;

	public EmailActivationCodeNotFoundException() {
		super("EMAIL_ACTIVATION_CODE_NOT_FOUND");
	}
}
