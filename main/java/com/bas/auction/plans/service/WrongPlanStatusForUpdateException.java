package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class WrongPlanStatusForUpdateException extends ApplException {
    public WrongPlanStatusForUpdateException() {
        super("WRONG_PLAN_STATUS_FOR_UPDATE");
    }
}
