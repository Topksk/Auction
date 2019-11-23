package com.bas.auction.neg.setting.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.dto.NegDiscount;

import java.util.Map;

public interface NegDiscountService {
    Map<Long, NegDiscount> findNegDiscounts(Long negId);

    void create(Negotiation neg);

    void copyNegDiscounts(User user, Long sourceNegId, Long destinationNegId);

    void delete(Long negId);
}
