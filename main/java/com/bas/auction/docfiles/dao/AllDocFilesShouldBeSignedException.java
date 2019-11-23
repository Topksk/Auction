package com.bas.auction.docfiles.dao;

import com.bas.auction.core.ApplException;

public class AllDocFilesShouldBeSignedException extends ApplException {

    public AllDocFilesShouldBeSignedException() {
        super("ALL_FILES_SHOULD_BE_SIGNED");
    }
}
