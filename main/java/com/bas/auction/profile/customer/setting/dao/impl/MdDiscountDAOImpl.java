package com.bas.auction.profile.customer.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.profile.customer.setting.dao.MdDiscountDAO;
import com.bas.auction.profile.customer.setting.dao.MdDiscountValDAO;
import com.bas.auction.profile.customer.setting.dto.MdDiscount;
import com.bas.auction.profile.customer.setting.dto.MdDiscountVal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
class MdDiscountDAOImpl implements MdDiscountDAO, GenericDAO<MdDiscount> {
    private final Logger logger = LoggerFactory.getLogger(MdDiscountDAOImpl.class);
    private final MdDiscountValDAO mdDiscountValDAO;
    private final MessageDAO messageDAO;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public MdDiscountDAOImpl(DaoJdbcUtil daoutil, MdDiscountValDAO mdDiscountValDAO, MessageDAO messageDAO) {
        this.daoutil = daoutil;
        this.mdDiscountValDAO = mdDiscountValDAO;
        this.messageDAO = messageDAO;
    }

    @Override
    public String getSqlPath() {
        return "customer_settings/md_discounts";
    }

    @Override
    public Class<MdDiscount> getEntityType() {
        return MdDiscount.class;
    }

    @Override
    public List<MdDiscount> findTenderDiscounts(Long settingId) {
        return findDiscountsForNegType(settingId, NegType.TENDER);
    }

    @Override
    public List<MdDiscount> findTender2Discounts(Long settingId) {
        return findDiscountsForNegType(settingId, NegType.TENDER2);
    }

    private List<MdDiscount> findDiscountsForNegType(Long settingId, NegType negType) {
        logger.debug("list discounts: settingId = {}, type = {}", settingId, negType);
        List<MdDiscount> list = daoutil.query(this, "list", settingId, negType.toString());
        for (MdDiscount nd : list) {
            nd.setValues(mdDiscountValDAO.findNegDiscountValues(nd.getDiscountId()));
        }
        return list;
    }

    protected MdDiscount create(User user, MdDiscount data) {
        Object[] values = {data.getSettingId(), data.getNegType(), data.getIsSystem(), data.getDescription(),
                data.isApplicableForGood(), data.isApplicableForWork(), data.isApplicableForService(),
                data.isApplicableForStage2(), data.getDiscountType(), data.getDiscountCode(),
                data.getDisplayInForm(), user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        data.setDiscountId((Long) kh.getKeys().get("discount_id"));
        if (data.getValues() != null) {
            List<MdDiscountVal> vals = new ArrayList<>(data.getValues().size());
            for (MdDiscountVal val : data.getValues()) {
                val.setDiscountId(data.getDiscountId());
                vals.add(mdDiscountValDAO.create(user, val));
            }
            data.setValues(vals);
        }
        return data;
    }

    protected MdDiscount update(User user, MdDiscount data) {
        Object[] values = {data.getDescription(), data.isApplicableForGood(), data.isApplicableForWork(),
                data.isApplicableForService(), data.isApplicableForStage2(), data.getDisplayInForm(),
                user.getUserId(), data.getDiscountId()};
        daoutil.update(this, values);
        if (data.getValues() != null) {
            List<MdDiscountVal> vals = new ArrayList<>(data.getValues().size());
            for (MdDiscountVal val : data.getValues()) {
                val.setDiscountId(data.getDiscountId());
                vals.add(mdDiscountValDAO.upsert(user, val));
            }
            data.setValues(vals);
        }
        return data;
    }

    private void initDomesticProducerDiscount(User user, long settingId, NegType negType) {
        MdDiscount disc = new MdDiscount();
        disc.setSettingId(settingId);
        disc.setNegType(negType.toString());
        disc.setDiscountCode("DOMESTIC_PRODUCER");
        disc.setDiscountType("YES_NO");
        disc.setIsSystem(true);
        String desc = messageDAO.getFromDb("DOMESTIC_PRODUCER", "RU");
        disc.setDescription(desc);
        disc.setApplicableForGood(true);
        disc.setApplicableForWork(false);
        disc.setApplicableForService(false);
        List<MdDiscountVal> vals = new ArrayList<>();
        MdDiscountVal val = new MdDiscountVal();
        val.setBoolValue(true);
        val.setDiscount(BigDecimal.ZERO);
        vals.add(val);
        val = new MdDiscountVal();
        val.setBoolValue(false);
        val.setDiscount(BigDecimal.ZERO);
        vals.add(val);
        disc.setValues(vals);
        create(user, disc);
    }

    private void initRangeDiscount(User user, long settingId, NegType negType, String discCode, String msgCode,
                                   boolean good, boolean work, boolean service) {
        MdDiscount discount = new MdDiscount();
        discount.setSettingId(settingId);
        discount.setNegType(negType.toString());
        discount.setDiscountCode(discCode);
        discount.setDiscountType("NUMBER_RANGE");
        discount.setIsSystem(true);
        String desc = messageDAO.getFromDb(msgCode, "RU");
        discount.setDescription(desc);
        discount.setApplicableForGood(good);
        discount.setApplicableForWork(work);
        discount.setApplicableForService(service);
        MdDiscountVal discountValue = new MdDiscountVal();
        discountValue.setNumberFrom(BigDecimal.ZERO);
        discountValue.setNumberTo(BigDecimal.valueOf(100));
        discountValue.setDiscount(BigDecimal.ZERO);
        List<MdDiscountVal> values = new ArrayList<>();
        values.add(discountValue);
        discount.setValues(values);
        create(user, discount);
    }

    private void initSysDiscounts(User user, Long settingId, NegType negType) {
        initDomesticProducerDiscount(user, settingId, negType);
        initRangeDiscount(user, settingId, negType, "WORK_SERVICE_LOCAL_CONTENT", "WORK_SERVICE_LOCAL_CONTENT", false,
                true, true);
        initRangeDiscount(user, settingId, negType, "EXPERIENCE", "EXPERIENCE", true, true, true);
    }

    @Override
    @SpringTransactional
    public void createSysNegDiscounts(User user, Long settingId) {
        initSysDiscounts(user, settingId, NegType.TENDER);
        initSysDiscounts(user, settingId, NegType.TENDER2);
    }

    @Override
    @SpringTransactional
    public void upsertTender(User user, Long settingId, java.util.List<MdDiscount> discounts) {
        upsert(user, settingId, NegType.TENDER, discounts);
    }

    @Override
    @SpringTransactional
    public void upsertTender2(User user, Long settingId, java.util.List<MdDiscount> discounts) {
        upsert(user, settingId, NegType.TENDER2, discounts);
    }

    private void upsert(User user, Long settingId, NegType negType, List<MdDiscount> discounts) {
        logger.debug("upsert neg discounts: settingId = {}, type = {}", settingId, negType);
        for (MdDiscount d : discounts) {
            d.setSettingId(settingId);
            d.setNegType(negType.toString());
            if (d.getDiscountId() == null || d.getDiscountId() < 0) {
                create(user, d);
            } else {
                update(user, d);
            }
        }
    }

    @Override
    @SpringTransactional
    public void delete(List<Long> ids) {
        List<Object[]> params = new ArrayList<>(ids.size());
        for (Long id : ids)
            params.add(new Object[]{id});
        mdDiscountValDAO.deleteDiscountVals(params);
        daoutil.batchDelete(this, params);
    }

}
