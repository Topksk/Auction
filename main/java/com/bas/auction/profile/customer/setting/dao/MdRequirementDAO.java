package com.bas.auction.profile.customer.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dto.MdRequirement;

import java.util.List;

public interface MdRequirementDAO {
    List<MdRequirement> findRfqReqs(Long settingId);

    List<MdRequirement> findAuctionReqs(Long settingId);

    List<MdRequirement> findTenderReqs(Long settingId);

    List<MdRequirement> findTender2Reqs(Long settingId);

    void createSysNegRequirements(User user, Long settingId);

    void reinitForeignCurrencyReq(User user, CustomerSetting set);

    void upsertRfqReqs(User user, Long settingId, List<MdRequirement> requirements);

    void upsertAuctionReqs(User user, Long settingId, List<MdRequirement> requirements);

    void upsertTenderReqs(User user, Long settingId, List<MdRequirement> requirements);

    void upsertTender2Reqs(User user, Long settingId, List<MdRequirement> requirements);

    void validateSettingsRequirements(Long settingId);
}
