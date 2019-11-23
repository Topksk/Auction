package com.bas.auction.bid.discount.service;

import com.bas.auction.core.ApplException;

public class BidDiscountCorrectionReasonRequiredException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3676120881879472038L;

	public BidDiscountCorrectionReasonRequiredException() {
		super("DISCOUNT_CORRECTION_REASON_REQ");
	}
}
