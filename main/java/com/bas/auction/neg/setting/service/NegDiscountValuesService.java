package com.bas.auction.neg.setting.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.dto.NegDiscount;
import com.bas.auction.neg.setting.dto.NegDiscountVal;

import java.util.List;

public interface NegDiscountValuesService {
    List<NegDiscountVal> findNegDiscountValues(Long negId);

    void create(Negotiation neg, List<NegDiscount> negDiscounts);

    void copyNegDiscountValues(User user, Long sourceNegId, Long destinationNegId);

    void delete(Long negId);
}
