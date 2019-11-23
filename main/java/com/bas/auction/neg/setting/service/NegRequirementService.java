package com.bas.auction.neg.setting.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

public interface NegRequirementService {
    void create(Negotiation neg);

    void copyNegRequirements(User user, Long sourceNegId, Long destinationNegId);

    void delete(Long negId);

    Long findForeignCurrencyControlRequirementId(Long negId);

    Long findDumpingControlRequirementId(Long negId);
}
