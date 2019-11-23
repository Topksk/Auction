package com.bas.auction.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApplException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int statusCode;
    private final List<String> codes;

    public void setParams(List<Map<String, String>> params) {
        this.params = params;
    }

    private List<Map<String, String>> params;

    public ApplException(List<String> codes) {
        super();
        this.codes = codes;
    }

    public ApplException(int statusCode, List<String> codes) {
        this(codes);
        this.statusCode = statusCode;
    }

    public ApplException(String code) {
        this(Collections.singletonList(code));
    }

    public ApplException(String code, List<Map<String, String>> params) {
        this(code);
        this.params = params;
    }

    public ApplException(int statusCode, String code) {
        this(code);
        this.statusCode = statusCode;
    }

    public List<String> getCodes() {
        return codes;
    }

    /**
     * HTTP code which will be send to client
     *
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Set HTTP code which will be send to client
     *
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<Map<String, String>> getParams() {
        return params;
    }
}
