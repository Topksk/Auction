package com.bas.auction.neg.setting.dao;

import com.bas.auction.neg.setting.dto.NegDiscount;

import java.util.List;

public interface NegDiscountDAO {
    List<NegDiscount> findNegDiscounts(Long negId);

    void insert(List<NegDiscount> negDiscounts);

    void delete(Long negId);
}
