package com.bas.auction.neg.voting.service;

import com.bas.auction.core.ApplException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfirmAllDiscountsException extends ApplException {
    public ConfirmAllDiscountsException(List<Integer> discountNotConfirmedLineNums) {
        super("CONFIRM_ALL_DISCOUNTS");
        List<Map<String, String>> params = discountNotConfirmedLineNums.stream()
                .map(line -> Collections.singletonMap("line_num", String.valueOf(line)))
                .collect(Collectors.toList());
        setParams(params);
    }
}