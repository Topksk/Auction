package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanPurchaseTypeNotInListException extends ApplException {
    public PlanPurchaseTypeNotInListException() {
        super("WRONG_PURCHASE_TYPE");
    }
}
