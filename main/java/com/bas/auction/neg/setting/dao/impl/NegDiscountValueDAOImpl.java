package com.bas.auction.neg.setting.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.neg.setting.dao.NegDiscountValueDAO;
import com.bas.auction.neg.setting.dto.NegDiscountVal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NegDiscountValueDAOImpl implements NegDiscountValueDAO, GenericDAO<NegDiscountVal> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegDiscountValueDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<NegDiscountVal> getEntityType() {
        return NegDiscountVal.class;
    }

    @Override
    public String getSqlPath() {
        return "neg_settings/neg_discounts/neg_discount_vals";
    }

    @Override
    public List<NegDiscountVal> findNegDiscountValues(Long negId) {
        return daoutil.query(this, "list", negId);
    }

    @Override
    public void insert(List<NegDiscountVal> negDiscountVals) {
        List<Object[]> values = negDiscountVals.stream()
                .map(this::mapToInsertValues)
                .collect(Collectors.toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] mapToInsertValues(NegDiscountVal negDiscountVal) {
        return new Object[]{negDiscountVal.getNegId(), negDiscountVal.getDiscountValId(), negDiscountVal.getDiscountId(),
                negDiscountVal.getBoolValue(), negDiscountVal.getNumberFrom(), negDiscountVal.getNumberTo(),
                negDiscountVal.getDiscount(), negDiscountVal.getCreatedBy(), negDiscountVal.getLastUpdatedBy()};
    }

    @Override
    public void delete(Long negId) {
        daoutil.delete(this, new Object[]{negId});
    }
}