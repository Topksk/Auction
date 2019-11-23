package com.bas.auction.neg.award.service;

import com.bas.auction.core.ApplException;

public class NegResumeReportRequiredException extends ApplException {
    public NegResumeReportRequiredException() {
        super("NEG_RESUME_REPORT_REQUIRED");
    }
}
