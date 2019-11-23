package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanKazContentNotInRangeException extends ApplException {
    public PlanKazContentNotInRangeException() {
        super("PERCENT_WRONG_RANGE");
    }
}
