package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class PlanValueTooLongException extends ApplException {
    public PlanValueTooLongException(String description) {
        super("VALUE_TOO_LONG_PARAM");
        setParams(singletonList(singletonMap("field_name", description)));
    }
}
