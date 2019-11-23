package com.bas.auction.profile.customer.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.bas.auction.profile.customer.setting.dao.MdDiscountDAO;
import com.bas.auction.profile.customer.setting.dao.MdRequirementDAO;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerSettingDAOImpl implements CustomerSettingDAO, GenericDAO<CustomerSetting> {
    private final Logger logger = LoggerFactory.getLogger(CustomerSettingDAOImpl.class);
    private final PlanColDAO planColDAO;
    private final MdRequirementDAO mdRequirementDAO;
    private final MdDiscountDAO mdDiscountDAO;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public CustomerSettingDAOImpl(DaoJdbcUtil daoutil, PlanColDAO planColDAO, MdRequirementDAO negRequirement,
                                  MdDiscountDAO negDiscount) {
        this.daoutil = daoutil;
        this.planColDAO = planColDAO;
        this.mdRequirementDAO = negRequirement;
        this.mdDiscountDAO = negDiscount;
    }

    @Override
    public String getSqlPath() {
        return "customer_settings";
    }

    @Override
    public Class<CustomerSetting> getEntityType() {
        return CustomerSetting.class;
    }

    private void initDetailsUsingDisplayedPlanCols(CustomerSetting set) {
        set.setPlanCols(planColDAO.findSettingDisplayedPlanColList(set.getSettingId()));
        initDetails(set);
    }

    private void initDetailsUsingAllPlanCols(CustomerSetting set) {
        set.setPlanCols(planColDAO.findSettingPlanColList(set.getSettingId()));
        initDetails(set);
    }

    private void initDetails(CustomerSetting set) {
        set.setRfqReqs(mdRequirementDAO.findRfqReqs(set.getSettingId()));
        set.setAuctionReqs(mdRequirementDAO.findAuctionReqs(set.getSettingId()));
        set.setTenderReqs(mdRequirementDAO.findTenderReqs(set.getSettingId()));
        set.setTenderDiscounts(mdDiscountDAO.findTenderDiscounts(set.getSettingId()));
        set.setTender2Reqs(mdRequirementDAO.findTender2Reqs(set.getSettingId()));
        set.setTender2Discounts(mdDiscountDAO.findTender2Discounts(set.getSettingId()));
    }

    @Override
    public CustomerSetting findMainWithoutDetails(Long customerId) {
        CustomerSetting set = daoutil.queryForObject(this, "get_main", customerId);
        if (set == null)
            logger.warn("customer main setting not found: {}", customerId);
        return set;
    }

    @Override
    public CustomerSetting findMainWithDetails(Long customerId) {
        logger.debug("get customer main setting: {}", customerId);
        CustomerSetting set = findMainWithoutDetails(customerId);
        if (set != null)
            initDetailsUsingDisplayedPlanCols(set);
        return set;
    }

    @Override
    public Long findMainId(Long customerId) {
        logger.debug("get customer main setting id: {}", customerId);
        Long id = daoutil.queryScalar(this, "get_main_id", customerId);
        if (id == null)
            logger.warn("customer main setting id not found: {}", customerId);
        return id;
    }

    @Override
    public List<CustomerSetting> findCustomerSettings(Long customerId) {
        logger.debug(" customer setting: {}", customerId);
        List<CustomerSetting> set = daoutil.query(this, "list", customerId);
        set.forEach(this::initDetailsUsingAllPlanCols);
        return set;
    }

    @Override
    public CustomerSetting findByIdWithoutDetails(Long settingId) {
        return daoutil.queryForObject(this, "get", settingId);
    }

    @Override
    public CustomerSetting findByIdWithDetails(Long settingId) {
        CustomerSetting set = findByIdWithoutDetails(settingId);
        if (set != null)
            initDetailsUsingDisplayedPlanCols(set);
        else
            logger.warn("customer setting not found: {}", settingId);
        return set;
    }

    @Override
    public CustomerSetting findAuctionSettings(Long settingId) {
        logger.debug("get customer setting: {}", settingId);
        return daoutil.queryForObject(this, "get_auction_settings", settingId);
    }

    @Override
    @SpringTransactional
    public CustomerSetting create(User user, Long customerId, String name) {
        logger.debug("create customer settings: customerId = {}", customerId);
        CustomerSetting data = new CustomerSetting();
        data.setCustomerId(customerId);
        data.setName(name);
        data.setMain(false);
        data.setRfqEnabled(true);
        data.setRfqAwardMethod("SECRETARY");
        data.setRfqForeignCurrencyControl(true);
        data.setTenderEnabled(true);
        data.setTenderForeignCurrencyControl(true);
        data.setPlansEnabled(true);
        data.setAuctionEnabled(true);
        data.setAuctionForeignCurrencyControl(true);
        data.setAuctionAwardMethod("SECRETARY");
        data.setAuctionDuration(null);
        data.setAuctionExtTimeLeft(null);
        data.setAuctionExtDuration(null);
        data.setAuctionExtNumber(null);
        data.setTender2Enabled(true);
        data.setTender2ForeignCurrencyControl(true);
        data.setItemCodeListType(null);
        data.setIntegrationSendAward(true);
        return create(user, data);
    }

    protected CustomerSetting create(User user, CustomerSetting setting) {
        Object[] values = {setting.getCustomerId(), setting.getName(), setting.isMain(), setting.isRfqEnabled(),
                setting.getRfqAwardMethod(), setting.isRfqForeignCurrencyControl(), setting.isTenderEnabled(),
                setting.isTenderForeignCurrencyControl(), setting.isAuctionEnabled(),
                setting.isAuctionForeignCurrencyControl(), setting.getAuctionAwardMethod(),
                setting.getAuctionDuration(), setting.getAuctionExtTimeLeft(), setting.getAuctionExtDuration(),
                setting.getAuctionExtNumber(), setting.isTender2Enabled(), setting.isTender2ForeignCurrencyControl(),
                setting.getItemCodeListType(), setting.isPlansEnabled(), setting.isIntegrationSendAward(),
                user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        Long settingId = (Long) kh.getKeys().get("setting_id");
        setting.setSettingId(settingId);
        createDetails(user, setting);
        return setting;
    }

    private void createDetails(User user, CustomerSetting setting) {
        Long settingId = setting.getSettingId();
        planColDAO.createPlanCols(user, settingId);
        mdRequirementDAO.createSysNegRequirements(user, settingId);
        mdDiscountDAO.createSysNegDiscounts(user, settingId);
    }

    @Override
    @SpringTransactional
    public void update(User user, CustomerSetting setting) {
        logger.debug("update customer settings: settingId = {}", setting.getSettingId());
        Object[] values = {setting.getName(), setting.isRfqEnabled(), setting.getRfqAwardMethod(),
                setting.isRfqForeignCurrencyControl(), setting.isTenderEnabled(),
                setting.isTenderForeignCurrencyControl(), setting.isAuctionEnabled(),
                setting.isAuctionForeignCurrencyControl(), setting.getAuctionAwardMethod(),
                setting.getAuctionDuration(), setting.getAuctionExtTimeLeft(), setting.getAuctionExtDuration(),
                setting.getAuctionExtNumber(), setting.isTender2Enabled(), setting.isTender2ForeignCurrencyControl(),
                setting.isPlansEnabled(), setting.getItemCodeListType(), setting.isIntegrationSendAward(), user.getUserId(), setting.getSettingId()};
        daoutil.update(this, values);
        updateDetails(user, setting);
    }

    private void updateDetails(User user, CustomerSetting setting) {
        updatePlanCols(user, setting);
        updateNegRequirements(user, setting);
        updateDiscounts(user, setting);
    }

    private void updatePlanCols(User user, CustomerSetting setting) {
        planColDAO.update(user, setting.getSettingId(), setting.getPlanCols());
    }

    private void updateNegRequirements(User user, CustomerSetting setting) {
        mdRequirementDAO.reinitForeignCurrencyReq(user, setting);
        Long settingId = setting.getSettingId();
        if (setting.getRfqReqs() != null)
            mdRequirementDAO.upsertRfqReqs(user, settingId, setting.getRfqReqs());
        if (setting.getAuctionReqs() != null)
            mdRequirementDAO.upsertAuctionReqs(user, settingId, setting.getAuctionReqs());
        if (setting.getTenderReqs() != null)
            mdRequirementDAO.upsertTenderReqs(user, settingId, setting.getTenderReqs());
        if (setting.getTender2Reqs() != null)
            mdRequirementDAO.upsertTender2Reqs(user, settingId, setting.getTender2Reqs());
    }

    private void updateDiscounts(User user, CustomerSetting setting) {
        Long settingId = setting.getSettingId();
        if (setting.getTenderDiscounts() != null)
            mdDiscountDAO.upsertTender(user, settingId, setting.getTenderDiscounts());
        if (setting.getTender2Discounts() != null)
            mdDiscountDAO.upsertTender2(user, settingId, setting.getTender2Discounts());
        if (setting.getDelDiscounts() != null)
            mdDiscountDAO.delete(setting.getDelDiscounts());
    }

    @Override
    @SpringTransactional
    public void makeMain(Long userId, Long settingId, Long customerId) {
        logger.debug("make main customer setting: {}", settingId);
        Object[] values = {settingId, userId, customerId};
        daoutil.dml(this, "make_main", values);
    }

    @Override
    public void validateCustomerMainSetting(Long customerId) {
        Long id = findMainId(customerId);
        if (id == null)
            throw new ApplException("NO_MAIN_CUSTOMER_SETTING");
        mdRequirementDAO.validateSettingsRequirements(id);
    }
}
