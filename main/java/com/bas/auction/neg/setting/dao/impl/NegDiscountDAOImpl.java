package com.bas.auction.neg.setting.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.neg.setting.dao.NegDiscountDAO;
import com.bas.auction.neg.setting.dto.NegDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NegDiscountDAOImpl implements NegDiscountDAO, GenericDAO<NegDiscount> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegDiscountDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<NegDiscount> getEntityType() {
        return NegDiscount.class;
    }

    @Override
    public String getSqlPath() {
        return "neg_settings/neg_discounts";
    }

    @Override
    public List<NegDiscount> findNegDiscounts(Long negId) {
        return daoutil.query(this, "get_neg_discounts", negId);
    }

    @Override
    public void insert(List<NegDiscount> negDiscounts) {
        List<Object[]> values = negDiscounts.stream()
                .map(this::mapToInsertValues)
                .collect(Collectors.toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] mapToInsertValues(NegDiscount negDiscount) {
        return new Object[]{negDiscount.getNegId(), negDiscount.getDiscountId(), negDiscount.getDescription(),
                negDiscount.getIsSystem(), negDiscount.isApplicableForGood(), negDiscount.isApplicableForWork(),
                negDiscount.isApplicableForService(), negDiscount.isApplicableForStage2(), negDiscount.getDiscountType(),
                negDiscount.getDiscountCode(), negDiscount.getDisplayInForm(),
                negDiscount.getCreatedBy(), negDiscount.getLastUpdatedBy()};
    }

    @Override
    public void delete(Long negId) {
        daoutil.delete(this, new Object[]{negId});
    }

}
