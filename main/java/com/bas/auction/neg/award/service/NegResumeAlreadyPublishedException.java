package com.bas.auction.neg.award.service;

import com.bas.auction.core.ApplException;

public class NegResumeAlreadyPublishedException extends ApplException {
    public NegResumeAlreadyPublishedException() {
        super("NEG_RESUME_ALREADY_PUBLISHED");
    }
}
