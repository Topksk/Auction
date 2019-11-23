package com.bas.auction.workday.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.workday.dto.Workday;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface WorkdayDAO {
	List<Map<String, Object>> findWorkdays(int year, int month);

	Boolean findIsWorkingDay(Date date);

	void create(User user, Workday workday);

	void update(User user, Workday workday);
}
