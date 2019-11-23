package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanPurchasePriorityNotInListException extends ApplException {
    public PlanPurchasePriorityNotInListException() {
        super("PURCHASE_PRIORITY_NOT_IN_LIST");
    }
}
