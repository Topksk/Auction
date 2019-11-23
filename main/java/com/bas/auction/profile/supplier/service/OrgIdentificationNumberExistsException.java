package com.bas.auction.profile.supplier.service;

import com.bas.auction.core.ApplException;

public class OrgIdentificationNumberExistsException extends ApplException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3588639800630166264L;

	public OrgIdentificationNumberExistsException() {
		super("IDENTIFICATION_NUMBER_EXISTS");
	}
}
