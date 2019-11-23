package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class ServiceDumpingThresholdRequiredException extends ApplException {
    public ServiceDumpingThresholdRequiredException() {
        super("SERVICE_DUMPING_THRESHOLD_REQUIRED");
    }
}
