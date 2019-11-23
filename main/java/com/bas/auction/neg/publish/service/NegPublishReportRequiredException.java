package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegPublishReportRequiredException extends ApplException {
    public NegPublishReportRequiredException() {
        super("NEG_PUBLISH_REPORT_REQUIRED");
    }
}
