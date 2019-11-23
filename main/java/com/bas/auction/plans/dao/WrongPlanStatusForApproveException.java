package com.bas.auction.plans.dao;

import com.bas.auction.core.ApplException;

public class WrongPlanStatusForApproveException extends ApplException {
    public WrongPlanStatusForApproveException() {
        super("WRONG_PLAN_STATUS_FOR_APPROVE");
    }
}
