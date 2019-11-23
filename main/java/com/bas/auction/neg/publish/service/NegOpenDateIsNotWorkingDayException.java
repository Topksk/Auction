package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegOpenDateIsNotWorkingDayException extends ApplException {
    public NegOpenDateIsNotWorkingDayException() {
        super("NEG_OPEN_DATE_IS_NOT_WORKING_DAY");
    }
}
