package com.bas.auction.plans.service;

import com.bas.auction.core.ApplException;

import java.util.List;
import java.util.Map;

public class PlanFieldRequiredException extends ApplException {
    public PlanFieldRequiredException(List<Map<String, String>> messageParams) {
        super("REQUIRED_FIELD_PARAM");
        setParams(messageParams);
    }
}
