package com.bas.auction.profile.customer.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.setting.dto.MdDiscountVal;

import java.util.List;

public interface MdDiscountValDAO {
    List<MdDiscountVal> findNegDiscountValues(long discountId);

    MdDiscountVal create(User user, MdDiscountVal data);

    MdDiscountVal update(User user, MdDiscountVal data);

    void deleteDiscountVals(List<Object[]> params);

    MdDiscountVal upsert(User user, MdDiscountVal val);

}
