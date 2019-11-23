package com.bas.auction.neg.setting.dao;

import com.bas.auction.neg.setting.dto.NegSetting;

public interface NegSettingDAO {
    NegSetting findNegSetting(Long negId);

    NegSetting insert(NegSetting negSetting);

    void delete(Long negId);

    String findAwardMethod(Long negId);

    Integer findAuctionDuration(Long negId);

    boolean isNegForeignCurrencyControlEnabled(Long negId);
}
