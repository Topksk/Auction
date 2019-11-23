package com.bas.auction.neg.setting.dao;


import com.bas.auction.neg.setting.dto.NegDiscountVal;

import java.util.List;

public interface NegDiscountValueDAO {
    List<NegDiscountVal> findNegDiscountValues(Long negId);

    void insert(List<NegDiscountVal> negDiscountVals);

    void delete(Long negId);
}
