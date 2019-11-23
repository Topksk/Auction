package com.bas.auction.core.utils.validation;

import com.bas.auction.core.ApplException;

public class WeakPasswordException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 386708674271392477L;

	public WeakPasswordException() {
		super("WEAK_PASSWORD");
	}
}
