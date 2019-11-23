package com.bas.auction.workday.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.workday.dao.WorkdayDAO;
import com.bas.auction.workday.dto.Workday;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class WorkdayDAOImpl implements WorkdayDAO, GenericDAO<Workday> {
    private final static Logger logger = LoggerFactory.getLogger(WorkdayDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public WorkdayDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "work_day";
    }

    @Override
    public Class<Workday> getEntityType() {
        return Workday.class;
    }

    @Override
    public List<Map<String, Object>> findWorkdays(int year, int month) {
        logger.debug("get workdays: year = {}, month = {}", year, month);
        return daoutil.queryForMapList(this, "find_work_days", year, month);
    }

    @Override
    public Boolean findIsWorkingDay(Date date) {
        return daoutil.queryScalar(this, "find_is_working_day", date);
    }

    @Override
    @SpringTransactional
    public void create(User user, Workday workday) {
        logger.debug("update workday: day={}, isWorking={}", workday.getDay(), workday.getIsWorking());
        Object[] values = {workday.getDay(), workday.getIsWorking(), workday.getDescription(), user.getUserId(),
                user.getUserId()};
        daoutil.insert(this, values);
    }

    @Override
    @SpringTransactional
    public void update(User user, Workday workday) {
        logger.debug("update workday: workday={}, isWorking={}", workday.getDay(), workday.getIsWorking());
        Object[] values = {workday.getDay(), workday.getIsWorking(), workday.getDescription(), user.getUserId(),
                workday.getDayId()};
        daoutil.update(this, values);
    }
}
