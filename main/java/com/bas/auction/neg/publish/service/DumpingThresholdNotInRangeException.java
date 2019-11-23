package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class DumpingThresholdNotInRangeException extends ApplException {
    public DumpingThresholdNotInRangeException() {
        super("DUMPING_THRESHOLD_NOT_IN_RANGE");
    }
}
