package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegAlreadyPublishedException extends ApplException {
    public NegAlreadyPublishedException() {
        super("NEG_ALREADY_PUBLISHED_EXCEPTION");
    }
}
