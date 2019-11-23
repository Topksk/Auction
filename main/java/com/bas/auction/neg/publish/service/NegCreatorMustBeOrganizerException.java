package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegCreatorMustBeOrganizerException extends ApplException {

    public NegCreatorMustBeOrganizerException() {
        super("NEG_CREATOR_MUST_BE_ORGANIZER");
    }
}
