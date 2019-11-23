package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class PlanFieldShouldBePositiveException extends ApplException {
    public PlanFieldShouldBePositiveException(String description) {
        super("NUMBER_SHOULD_POSITIVE");
        setParams(singletonList(singletonMap("field_name", description)));
    }
}
