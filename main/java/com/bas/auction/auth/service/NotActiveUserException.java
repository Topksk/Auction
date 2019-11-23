package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class NotActiveUserException extends ApplException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1996312537146226295L;

	public NotActiveUserException() {
		super("NOT_ACTIVE_USER");
	}

}
