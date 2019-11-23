package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class GoodDumpingThresholdRequiredException extends ApplException {

    public GoodDumpingThresholdRequiredException() {
        super("GOOD_DUMPING_THRESHOLD_REQUIRED");
    }
}
