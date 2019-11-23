package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class EmailValidationException extends ApplException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6761563909326094320L;

	public EmailValidationException() {
		super("INVALID_EMAIL");
	}

}
