package com.bas.auction.profile.employee.service;

import com.bas.auction.core.ApplException;

public class IndividualsCanRegisterOnlyOneEmployeeException extends ApplException {
    public IndividualsCanRegisterOnlyOneEmployeeException() {
        super("INDIVIDUALS_CAN_REGISTER_ONLY_ONE_EMPLOYEE");
    }
}
