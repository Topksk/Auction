package com.bas.auction.workday.dto;

import java.util.Date;

import com.bas.auction.core.dto.AuditableRow;

public class Workday extends AuditableRow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4087703802433055773L;
	private long dayId;
	private Date day;
	private Boolean isWorking;
	private String description;

	public long getDayId() {
		return dayId;
	}

	public void setDayId(long dayId) {
		this.dayId = dayId;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public Boolean getIsWorking() {
		return isWorking;
	}

	public void setIsWorking(Boolean isWorking) {
		this.isWorking = isWorking;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
