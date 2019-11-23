package com.bas.auction.neg.draft.service;

import com.bas.auction.core.ApplException;

public class CantDeleteSecondStageException extends ApplException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6639449460105711468L;

	public CantDeleteSecondStageException() {
		super("CANT_DELETE_SECOND_STAGE");
	}
}
