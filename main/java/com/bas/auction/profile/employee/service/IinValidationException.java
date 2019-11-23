package com.bas.auction.profile.employee.service;

import com.bas.auction.core.ApplException;

public class IinValidationException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 133005731200916664L;

	public IinValidationException() {
		super("INVALID_IIN");
	}
}
