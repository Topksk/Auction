package com.bas.auction.profile.customer.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;

import java.util.List;

public interface CustomerSettingDAO {
    CustomerSetting findMainWithDetails(Long customerId);

    CustomerSetting findMainWithoutDetails(Long customerId);

    Long findMainId(Long customerId);

    CustomerSetting findAuctionSettings(Long settingId);

    List<CustomerSetting> findCustomerSettings(Long customerId);

    CustomerSetting findByIdWithoutDetails(Long settingId);

    CustomerSetting findByIdWithDetails(Long settingId);

    CustomerSetting create(User user, Long customerId, String name);

    void update(User user, CustomerSetting setting);

    void makeMain(Long userId, Long settingId, Long customerId);

    void validateCustomerMainSetting(Long customerId);
}