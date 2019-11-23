package com.bas.auction.plans.dao;

import com.bas.auction.plans.dto.PlanImport;

public interface PlanImporter {

	void parseImport(PlanImport pi) throws Exception;

}
