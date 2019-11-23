package com.bas.auction.profile.supplier.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.supplier.dto.SupplierSetting;
import com.bas.auction.profile.supplier.setting.dao.NegNotificationDAO;
import com.bas.auction.profile.supplier.setting.dao.SupplierSettingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SupplierSettingDAOImpl implements SupplierSettingDAO, GenericDAO<SupplierSetting> {
    private final Logger logger = LoggerFactory.getLogger(SupplierSettingDAOImpl.class);
    private final NegNotificationDAO negNotificationDAO;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public SupplierSettingDAOImpl(NegNotificationDAO negNotificationDAO, DaoJdbcUtil daoutil) {
        this.negNotificationDAO = negNotificationDAO;
        this.daoutil = daoutil;
    }

    @Override
    public Class<SupplierSetting> getEntityType() {
        return SupplierSetting.class;
    }

    @Override
    public String getSqlPath() {
        return "supplier_settings";
    }

    @Override
    public List<SupplierSetting> findSupplierSettings(Long supplierId) {
        logger.debug("list supplier settings: {}", supplierId);
        List<SupplierSetting> settings = daoutil.query(this, "list", supplierId);
        settings.forEach(this::initDetails);
        return settings;
    }

    @Override
    public SupplierSetting findMainSupplierSetting(Long supplierId) {
        logger.debug("get main supplier setting: {}", supplierId);
        SupplierSetting setting = daoutil.queryForObject(this, "get_main", supplierId);
        if (setting != null) {
            initDetails(setting);
        } else {
            setting = new SupplierSetting();
            setting.setSupplierId(supplierId);
        }
        return setting;
    }

    private void initDetails(SupplierSetting setting) {
        setting.setNotifications(negNotificationDAO.findNotifications(setting.getSettingId()));
    }

    @Override
    @SpringTransactional
    public SupplierSetting create(User user, SupplierSetting setting) {
        logger.debug("create supplier settings: supplierId = {}", user.getSupplierId());
        Object[] values = {user.getSupplierId(), setting.getName(), user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        Long settingId = (Long) kh.getKeys().get("setting_id");
        setting.setSettingId(settingId);
        createDetail(user, setting);
        return setting;
    }

    private void createDetail(User user, SupplierSetting setting) {
        Long settingId = setting.getSettingId();
        negNotificationDAO.upsert(settingId, user, setting.getNotifications());
    }

    @Override
    @SpringTransactional
    public void update(User user, SupplierSetting setting) {
        logger.debug("update supplier settings: settingId = {}", setting.getSettingId());
        Object[] values = {setting.getName(), user.getUserId(), setting.getSettingId()};
        daoutil.update(this, values);
        updateDetail(user, setting);
    }

    private void updateDetail(User user, SupplierSetting setting) {
        Long settingId = setting.getSettingId();
        negNotificationDAO.upsert(settingId, user, setting.getNotifications());
    }
}
