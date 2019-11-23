package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

public class PlanFinancialYearshouldNotBePastException extends ApplException {
    public PlanFinancialYearshouldNotBePastException() {
        super("FINANCIAL_YEAR_NOT_PAST");
    }
}
