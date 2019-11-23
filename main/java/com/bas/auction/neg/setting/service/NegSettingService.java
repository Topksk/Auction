package com.bas.auction.neg.setting.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

public interface NegSettingService {
    void create(Negotiation neg);

    void copyNegSettings(User user, Long sourceNegId, Long destinationNegId);

    void delete(Long negId);

    String findNegAwardMethod(Long negId);

    Integer findAuctionDuration(Long negId);

    boolean isNegForeignCurrencyControlEnabled(Long negId);
}
