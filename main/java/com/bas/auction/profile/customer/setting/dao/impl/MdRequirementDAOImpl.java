package com.bas.auction.profile.customer.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.profile.customer.setting.dao.MdRequirementDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dto.MdRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MdRequirementDAOImpl implements MdRequirementDAO, GenericDAO<MdRequirement> {
    private final static Logger logger = LoggerFactory.getLogger(MdRequirementDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final MessageDAO messageDAO;

    @Autowired
    public MdRequirementDAOImpl(DaoJdbcUtil daoutil, MessageDAO messageDAO) {
        this.daoutil = daoutil;
        this.messageDAO = messageDAO;
    }

    @Override
    public String getSqlPath() {
        return "customer_settings/md_requirements";
    }

    @Override
    public Class<MdRequirement> getEntityType() {
        return MdRequirement.class;
    }

    private Object[] fieldVals(User user, MdRequirement data) {
        return new Object[]{data.getSettingId(), data.getNegType(), data.getReqType(), data.getIsSystem(),
                data.getDescription(), data.isApplicableForStage1(), data.isApplicableForStage2(), user.getUserId(),
                user.getUserId()};
    }

    private MdRequirement foreignCurrencyControlReq(long settingId, NegType negType, String desc) {
        MdRequirement req = new MdRequirement();
        req.setSettingId(settingId);
        req.setNegType(negType.toString());
        req.setReqType("FOREIGN_CURRENCY_CONTROL");
        req.setDescription(desc);
        req.setIsSystem(true);
        return req;
    }

    private List<Object[]> foreignCurrencyControlReqs(User user, long settingId) {
        String desc = messageDAO.getFromDb("FOREIGN_CURRENCY_CONTROL_REQ", "RU");
        List<Object[]> values = new ArrayList<>();
        for (NegType negType : NegType.values()) {
            MdRequirement req = foreignCurrencyControlReq(settingId, negType, desc);
            values.add(fieldVals(user, req));
        }
        return values;
    }

    private List<Object[]> dumpingControlReqs(User user, long settingId) {
        String msg = messageDAO.getFromDb("DUMPING_CONTROL", "RU");
        List<Object[]> values = new ArrayList<>();
        NegType[] types = {NegType.TENDER, NegType.TENDER2};
        for (NegType type : types) {
            MdRequirement req = new MdRequirement();
            req.setSettingId(settingId);
            req.setReqType("DUMPING_CONTROL");
            req.setDescription(msg);
            req.setNegType(type.toString());
            req.setIsSystem(true);
            values.add(fieldVals(user, req));
        }
        return values;
    }

    @Override
    @SpringTransactional
    public void createSysNegRequirements(User user, Long settingId) {
        List<Object[]> insertValues = foreignCurrencyControlReqs(user, settingId);
        insertValues.addAll(dumpingControlReqs(user, settingId));
        daoutil.batchInsert(this, insertValues);
    }

    private boolean exists(long settingId, NegType negType) {
        Object[] params = {settingId, negType.toString()};
        return daoutil.exists(this, "foreign_curr_req_exists", params);
    }

    @Override
    @SpringTransactional
    public void reinitForeignCurrencyReq(User user, CustomerSetting set) {
        String desc = messageDAO.getFromDb("FOREIGN_CURRENCY_CONTROL_REQ", "RU");
        long settingId = set.getSettingId();
        List<Object[]> insertValues = new ArrayList<>();
        List<Object[]> deleteValues = new ArrayList<>();
        for (NegType negType : NegType.values()) {
            boolean exists = exists(settingId, negType);
            boolean enabled = isForeignCurrencyControlEnabled(set, negType);
            if (!exists && enabled) {
                MdRequirement req = foreignCurrencyControlReq(settingId, negType, desc);
                insertValues.add(fieldVals(user, req));
            } else if (exists && !enabled) {
                deleteValues.add(new Object[]{settingId, negType.toString()});
            }
        }
        if (!insertValues.isEmpty()) {
            daoutil.batchInsert(this, insertValues);
        }
        if (!deleteValues.isEmpty()) {
            daoutil.batchDelete(this, deleteValues);
        }
    }

    private boolean isForeignCurrencyControlEnabled(CustomerSetting set, NegType negType) {
        switch (negType) {
            case RFQ:
                return set.isRfqForeignCurrencyControl();
            case AUCTION:
                return set.isAuctionForeignCurrencyControl();
            case TENDER:
                return set.isTenderForeignCurrencyControl();
            case TENDER2:
                return set.isTender2ForeignCurrencyControl();
            default:
                return false;
        }
    }

    @Override
    public List<MdRequirement> findRfqReqs(Long settingId) {
        return findReqsForNegType(settingId, NegType.RFQ);
    }

    @Override
    public List<MdRequirement> findAuctionReqs(Long settingId) {
        return findReqsForNegType(settingId, NegType.AUCTION);
    }

    @Override
    public List<MdRequirement> findTenderReqs(Long settingId) {
        return findReqsForNegType(settingId, NegType.TENDER);
    }

    @Override
    public List<MdRequirement> findTender2Reqs(Long settingId) {
        return findReqsForNegType(settingId, NegType.TENDER2);
    }

    private List<MdRequirement> findReqsForNegType(Long settingId, NegType negType) {
        return daoutil.query(this, "list", settingId, negType.toString());
    }

    @Override
    @SpringTransactional
    public void upsertRfqReqs(User user, Long settingId, List<MdRequirement> requirements) {
        upsert(user, settingId, NegType.RFQ, requirements);
    }

    @Override
    @SpringTransactional
    public void upsertAuctionReqs(User user, Long settingId, List<MdRequirement> requirements) {
        upsert(user, settingId, NegType.AUCTION, requirements);
    }

    @Override
    @SpringTransactional
    public void upsertTenderReqs(User user, Long settingId, List<MdRequirement> requirements) {
        upsert(user, settingId, NegType.TENDER, requirements);
    }

    @Override
    @SpringTransactional
    public void upsertTender2Reqs(User user, Long settingId, List<MdRequirement> requirements) {
        upsert(user, settingId, NegType.TENDER2, requirements);
    }

    private void upsert(User user, Long settingId, NegType negType, List<MdRequirement> requirements) {
        logger.debug("upsert neg reqs: settingId = {}, type = {}", settingId, negType);
        List<Object[]> values1 = new ArrayList<>();
        List<Object[]> values2 = new ArrayList<>();
        for (MdRequirement req : requirements) {
            req.setSettingId(settingId);
            req.setNegType(negType.toString());
            if (req.getRequirementId() <= 0) {
                values1.add(fieldVals(user, req));
            } else {
                Object[] vals = {req.getDescription(), req.isApplicableForStage1(), req.isApplicableForStage2(),
                        user.getUserId(), req.getRequirementId()};
                values2.add(vals);
            }
        }
        if (!values1.isEmpty()) {
            daoutil.batchInsert(this, values1);
        }
        if (!values2.isEmpty()) {
            daoutil.batchUpdate(this, values2);
        }
    }

    @Override
    public void validateSettingsRequirements(Long settingId) {
        List<String> msg = new ArrayList<>();
        for (NegType negType : NegType.values()) {
            Object[] params = {settingId, negType.toString()};
            if (!daoutil.exists(this, "exists", params)) {
                msg.add(negType.toString() + "_NEG_REQUIREMENTS_REQUIRED");
            }
        }
        if (!msg.isEmpty())
            throw new ApplException(msg);
    }
}
