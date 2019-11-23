package com.bas.auction.plans.dao;

import com.bas.auction.core.ApplException;

public class WrongPlanStatusForDeleteException extends ApplException {
    public WrongPlanStatusForDeleteException() {
        super("WRONG_PLAN_STATUS_FOR_DELETE");
    }
}
