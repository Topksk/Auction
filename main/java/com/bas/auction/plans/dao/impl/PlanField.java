package com.bas.auction.plans.dao.impl;

import org.apache.poi.ss.usermodel.Cell;

import com.bas.auction.plans.dto.PlanCol;

class PlanField {
	final PlanCol col;
	final Cell cell;
	final int index;

	public PlanField(PlanCol col, Cell cell, int index) {
		this.col = col;
		this.cell = cell;
		this.index = index;
	}
}