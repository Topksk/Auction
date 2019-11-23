package com.bas.auction.worker;

import com.bas.auction.plans.dao.PlanImportWorker;
import com.bas.auction.plans.dao.PlanImporter;
import com.bas.auction.plans.dto.PlanImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PlanImportWorkerImpl implements PlanImportWorker {
	private final static Logger logger = LoggerFactory.getLogger(PlanImportWorkerImpl.class);
	private final PlanImporter planImportDAO;

	@Autowired
	public PlanImportWorkerImpl(PlanImporter planImportDAO) {
		this.planImportDAO = planImportDAO;
	}

	@Override
	@Async("planImportTaskExecutor")
	public void submit(PlanImport planImport) {
		Thread.currentThread().setName("plan_import: " + planImport.id);
		logger.debug("Start plan file import task: {}", planImport.id);
		/*try {
			planImportDAO.parseImport(planImport);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("Plan file import finished: {}", planImport.id);
		*/
	}
}
