package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegCloseDateIsNotWorkingDayException extends ApplException {
    public NegCloseDateIsNotWorkingDayException() {
        super("NEG_CLOSE_DATE_IS_NOT_WORKING_DAY");
    }
}
