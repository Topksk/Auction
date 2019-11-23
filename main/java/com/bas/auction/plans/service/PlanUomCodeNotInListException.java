package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanUomCodeNotInListException extends ApplException {
    public PlanUomCodeNotInListException() {
        super("UOM_CODE_NOT_IN_LIST");
    }
}
