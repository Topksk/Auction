package com.bas.auction.core;

import javax.servlet.http.HttpServletResponse;

public class AccessDeniedException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8220874206694136224L;

	public AccessDeniedException() {
		super(HttpServletResponse.SC_FORBIDDEN, "ACCESS_DENIED");
	}
}
