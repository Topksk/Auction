package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanPurchaseMethodNotInListException extends ApplException {
    public PlanPurchaseMethodNotInListException() {
        super("WRONG_PURCHASE_METHOD_GENERAL");
    }
}
