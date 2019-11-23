package com.bas.auction.neg.draft.service;

import com.bas.auction.core.ApplException;

public class CantUpdateNotDraftNegException extends ApplException {
    public CantUpdateNotDraftNegException() {
        super("CANT_UPDATE_NOT_DRAFT_NEG");
    }
}
