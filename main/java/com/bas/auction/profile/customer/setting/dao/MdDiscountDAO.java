package com.bas.auction.profile.customer.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.setting.dto.MdDiscount;

import java.util.List;

public interface MdDiscountDAO {
    List<MdDiscount> findTenderDiscounts(Long settingId);

    List<MdDiscount> findTender2Discounts(Long settingId);

    void createSysNegDiscounts(User user, Long settingId);

    void upsertTender(User user, Long settingId, List<MdDiscount> discounts);

    void upsertTender2(User user, Long settingId, List<MdDiscount> discounts);

    void delete(List<Long> ids);
}
