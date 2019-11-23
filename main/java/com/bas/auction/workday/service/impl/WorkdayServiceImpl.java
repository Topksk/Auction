package com.bas.auction.workday.service.impl;

import com.bas.auction.workday.dao.WorkdayDAO;
import com.bas.auction.workday.service.WorkdayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class WorkdayServiceImpl implements WorkdayService {

    private final WorkdayDAO workdayDAO;

    @Autowired
    public WorkdayServiceImpl(WorkdayDAO workdayDAO) {
        this.workdayDAO = workdayDAO;
    }

    @Override
    public boolean isWorkingDay(Date date, int startHour) {
        if (!isWorkingHour(date, startHour))
            return false;
        Boolean isWorkingDay = workdayDAO.findIsWorkingDay(date);
        if (isWorkingDay == null)
            return !isWeekend(date);
        else
            return isWorkingDay;
    }

    protected boolean isWorkingHour(Date date, int startHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean tooEarly = hourOfDay < startHour;
        boolean tooLate = hourOfDay > 17 && minute > 0;
        return !(tooEarly || tooLate);
    }

    protected boolean isWeekend(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY;
    }
}