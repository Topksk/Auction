package com.bas.auction.profile.customer.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.customer.setting.dto.MdDiscountVal;
import com.bas.auction.profile.customer.setting.dao.MdDiscountValDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MdDiscountValDAOImpl implements MdDiscountValDAO, GenericDAO<MdDiscountVal> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public MdDiscountValDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;

    }

    @Override
    public String getSqlPath() {
        return "customer_settings/md_discounts/md_discount_values";
    }

    @Override
    public Class<MdDiscountVal> getEntityType() {
        return MdDiscountVal.class;
    }

    @Override
    public List<MdDiscountVal> findNegDiscountValues(long discountId) {
        return daoutil.query(this, "list", discountId);
    }

    @Override
    @SpringTransactional
    public MdDiscountVal create(User user, MdDiscountVal data) {
        Object[] values = {data.getDiscountId(), data.getBoolValue(), data.getNumberFrom(), data.getNumberTo(),
                data.getDiscount(), user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        data.setDiscountValId((long) kh.getKeys().get("discount_val_id"));
        return data;
    }

    @Override
    @SpringTransactional
    public MdDiscountVal update(User user, MdDiscountVal data) {
        Object[] values = {data.getBoolValue(), data.getNumberFrom(), data.getNumberTo(), data.getDiscount(),
                user.getUserId(), data.getDiscountValId()};
        daoutil.update(this, values);
        return data;
    }

    @Override
    @SpringTransactional
    public void deleteDiscountVals(List<Object[]> params) {
        daoutil.batchDelete(this, params);
    }

    @Override
    @SpringTransactional
    public MdDiscountVal upsert(User user, MdDiscountVal val) {
        if (val.getDiscountValId() < 0) {
            return create(user, val);
        } else {
            return update(user, val);
        }
    }

}
