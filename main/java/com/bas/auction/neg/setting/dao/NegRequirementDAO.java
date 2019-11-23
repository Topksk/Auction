package com.bas.auction.neg.setting.dao;

import com.bas.auction.neg.setting.dto.NegRequirement;

import java.util.List;

public interface NegRequirementDAO {
    List<NegRequirement> findNegRequirements(Long negId);

    void insert(List<NegRequirement> negRequirements);

    void delete(Long negId);

    Long findForeignCurrencyControlRequirementId(Long negId);

    Long findDumpingControlRequirementId(Long negId);
}
