package com.bas.auction.neg.setting.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.neg.setting.dao.NegRequirementDAO;
import com.bas.auction.neg.setting.dto.NegRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class NegRequirementDAOImpl implements NegRequirementDAO, GenericDAO<NegRequirement> {
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegRequirementDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public Class<NegRequirement> getEntityType() {
        return NegRequirement.class;
    }

    @Override
    public String getSqlPath() {
        return "neg_settings/neg_requirements";
    }

    @Override
    public List<NegRequirement> findNegRequirements(Long negId) {
        return daoutil.query(this, "list", negId);
    }

    @Override
    public void insert(List<NegRequirement> negRequirements) {
        List<Object[]> values = negRequirements.stream()
                .map(this::mapToInsertValues)
                .collect(Collectors.toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] mapToInsertValues(NegRequirement negRequirement) {
        return new Object[]{negRequirement.getNegId(), negRequirement.getRequirementId(), negRequirement.getDescription(),
                negRequirement.getIsSystem(), negRequirement.getReqType(), negRequirement.isApplicableForStage1(),
                negRequirement.isApplicableForStage2(), negRequirement.getCreatedBy(), negRequirement.getLastUpdatedBy()};
    }

    @Override
    public void delete(Long negId) {
        daoutil.delete(this, new Object[]{negId});
    }

    @Override
    public Long findForeignCurrencyControlRequirementId(Long negId) {
        return daoutil.queryScalar(this, "get_foreign_currency_control_id", negId);
    }

    @Override
    public Long findDumpingControlRequirementId(Long negId) {
        return daoutil.queryScalar(this, "get_dumping_control_id", negId);
    }
}
