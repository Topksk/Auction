package com.bas.auction.workday.service;


import java.util.Date;

public interface WorkdayService {
    boolean isWorkingDay(Date date, int startHour);
}
