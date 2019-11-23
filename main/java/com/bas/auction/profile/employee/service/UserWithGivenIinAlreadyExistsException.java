package com.bas.auction.profile.employee.service;

import com.bas.auction.core.ApplException;

public class UserWithGivenIinAlreadyExistsException extends ApplException {
    public UserWithGivenIinAlreadyExistsException() {
        super("USER_WITH_GIVEN_IIN_EXISTS");
    }
}
