package com.bas.auction.profile.customer.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlanColDAOImpl implements PlanColDAO, GenericDAO<PlanCol> {
    private final Logger logger = LoggerFactory.getLogger(PlanColDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public PlanColDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "customer_settings/plan_cols";
    }

    @Override
    public Class<PlanCol> getEntityType() {
        return PlanCol.class;
    }

    @Override
    public List<PlanCol> findSettingPlanColList(Long settingId) {
        logger.debug("list all plan cols: {}", settingId);
        List<PlanCol> cols = daoutil.query(this, "list_all", settingId);
        if (cols.isEmpty())
            logger.warn("no plan cols found: {}", settingId);
        return cols;
    }

    @Override
    public List<PlanCol> findSettingDisplayedPlanColList(Long settingId) {
        logger.debug("list plan cols: {}", settingId);
        List<PlanCol> cols = daoutil.query(this, "list", settingId);
        if (cols.isEmpty())
            logger.warn("no plan cols found: {}", settingId);
        return cols;
    }

    @Override
    @SpringTransactional
    public void createPlanCols(User user, Long settingId) {
        Object[] values = {settingId, user.getUserId(), user.getUserId()};
        daoutil.dml(this, "init_cust_new_setting_plan_cols", values);
    }

    @Override
    @SpringTransactional
    public int[] update(User user, Long settingId, List<PlanCol> cols) {
        List<Object[]> values = new ArrayList<>(cols.size());
        for (PlanCol col : cols) {
            Object[] vals = {col.getRequired(), col.getDisplayInForm(), col.getDisplayInTemplate(), col.getEditable(),
                    col.getOrderNum(), user.getUserId(), col.getColId(), settingId};
            values.add(vals);
        }
        return daoutil.batchUpdate(this, values);
    }
}
