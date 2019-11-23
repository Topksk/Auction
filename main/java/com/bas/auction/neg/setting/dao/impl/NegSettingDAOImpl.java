package com.bas.auction.neg.setting.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.setting.dao.NegSettingDAO;
import com.bas.auction.neg.setting.dto.NegSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class NegSettingDAOImpl implements NegSettingDAO, GenericDAO<NegSetting> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegSettingDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<NegSetting> getEntityType() {
        return NegSetting.class;
    }

    @Override
    public String getSqlPath() {
        return "neg_settings";
    }

    @Override
    public NegSetting findNegSetting(Long negId) {
        return daoutil.queryForObject(this, "get", negId);
    }

    @Override
    @SpringTransactional
    public NegSetting insert(NegSetting negSetting) {
        Object[] values = {negSetting.getNegId(), negSetting.getAwardMethod(), negSetting.isForeignCurrencyControl(),
                negSetting.getAuctionDuration(), negSetting.getAuctionExtTimeLeft(), negSetting.getAuctionExtDuration(),
                negSetting.getAuctionExtNumber(), negSetting.getCreatedBy(), negSetting.getLastUpdatedBy()};
        daoutil.insert(this, values);
        return negSetting;
    }

    @Override
    @SpringTransactional
    public void delete(Long negId) {
        daoutil.delete(this, new Object[]{negId});
    }

    @Override
    public String findAwardMethod(Long negId) {
        return daoutil.queryScalar(this, "get_award_method", negId);
    }

    @Override
    public Integer findAuctionDuration(Long negId) {
        return daoutil.queryScalar(this, "get_auction_duration", negId);
    }

    @Override
    public boolean isNegForeignCurrencyControlEnabled(Long negId) {
        return daoutil.queryScalar(this, "get_foreign_currency_control", negId);
    }
}