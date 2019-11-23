package com.bas.auction.auth.service;

import com.bas.auction.core.ApplException;

public class NoMainUserException extends ApplException {
    public NoMainUserException() {
        super("MAIN_USER_REQUIRED");
    }
}
