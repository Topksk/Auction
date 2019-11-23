package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class WorkDumpingThresholdRequiredException extends ApplException {
    public WorkDumpingThresholdRequiredException() {
        super("WORK_DUMPING_THRESHOLD_REQUIRED");
    }
}
