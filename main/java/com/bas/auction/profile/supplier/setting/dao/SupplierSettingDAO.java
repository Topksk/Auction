package com.bas.auction.profile.supplier.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.supplier.dto.SupplierSetting;

import java.util.List;

/**
 * Created by bayangali.nauryz on 18.12.2015.
 */
public interface SupplierSettingDAO {
    List<SupplierSetting> findSupplierSettings(Long supplierId);

    SupplierSetting findMainSupplierSetting(Long supplierId);

    SupplierSetting create(User supplier, SupplierSetting setting);

    void update(User user, SupplierSetting setting);
}
