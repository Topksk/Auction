package com.bas.auction.docfiles.dao;

import com.bas.auction.core.ApplException;

public class SignatureTooBigException extends ApplException {
    public SignatureTooBigException() {
        super("SIGNATURE_TOO_BIG");
    }
}
