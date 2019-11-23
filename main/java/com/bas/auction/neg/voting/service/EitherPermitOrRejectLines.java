package com.bas.auction.neg.voting.service;

import com.bas.auction.core.ApplException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EitherPermitOrRejectLines extends ApplException {

    public EitherPermitOrRejectLines(List<Integer> negInvalidPermissionBidLines) {
        super("EITHER_PERMIT_OR_REJECT_LINE");
        List<Map<String, String>> params = negInvalidPermissionBidLines.stream()
                .map(line -> Collections.singletonMap("line_num", String.valueOf(line)))
                .collect(Collectors.toList());
        setParams(params);
    }
}
