package com.bas.auction.neg.publish.service;

import com.bas.auction.core.ApplException;

public class NegUnlockDateIsNotWorkingDayException extends ApplException {
    public NegUnlockDateIsNotWorkingDayException() {
        super("NEG_UNLOCK_DATE_IS_NOT_WORKING_DAY");
    }
}
