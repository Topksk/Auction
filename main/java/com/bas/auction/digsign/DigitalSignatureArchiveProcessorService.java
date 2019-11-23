package com.bas.auction.digsign;


import javax.servlet.http.HttpServletRequest;

public interface DigitalSignatureArchiveProcessorService {
    void processArchiveWithSignedFile(HttpServletRequest req, long userId) throws Exception;

    void processArchiveWithSignature(HttpServletRequest req, long userId) throws Exception;

    void processArchiveWithUnsignedFile(HttpServletRequest req, long userId) throws Exception;
}