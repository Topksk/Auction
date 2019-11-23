package com.bas.auction.neg.service;

import com.bas.auction.core.ApplException;

public class NegotiationNotificationSendingException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6443575342031788689L;

	public NegotiationNotificationSendingException() {
		super("NEG_NOTIF_SENDING_EXCEPTION");
	}
}
