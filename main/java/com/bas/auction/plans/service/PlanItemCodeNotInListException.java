package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanItemCodeNotInListException extends ApplException {
    public PlanItemCodeNotInListException() {
        super("ITEM_CODE_NOT_IN_LIST");
    }
}
