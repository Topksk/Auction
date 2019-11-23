package com.bas.auction.core.utils.validation;

import com.bas.auction.core.ApplException;

public class ShortPasswordException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 428202315861473450L;

	public ShortPasswordException() {
		super("SHORT_PASSWORD");
	}
}
