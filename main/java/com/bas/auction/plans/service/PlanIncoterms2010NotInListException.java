package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanIncoterms2010NotInListException extends ApplException {
    public PlanIncoterms2010NotInListException() {
        super("INCOTERMS2010_NOT_IN_LIST");
    }
}
