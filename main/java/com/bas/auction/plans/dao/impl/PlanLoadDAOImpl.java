package com.bas.auction.plans.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.plans.dao.PlanLoadDAO;
import com.bas.auction.plans.dto.Plan;
import com.google.gson.Gson;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanLoadDAOImpl implements PlanLoadDAO, GenericDAO<Plan> {
	private final Logger logger = LoggerFactory.getLogger(PlanLoadDAOImpl.class);
	private final DaoJdbcUtil daoutil;
	private final Gson gson;
	private BulkRequestBuilder bulkRequest;
	private Client client;

	@Autowired
	public PlanLoadDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
		this.gson = Utils.getGsonForSearchIndex();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	@Override
	public Class<Plan> getEntityType() {
		return Plan.class;
	}

	private List<Plan> findAllPlans() {
		return daoutil.query(this, "list_all_plans");
	}

	@Override
	public void indexAllPlans(Client client) {
		logger.info("start indexing all plans");
		this.client = client;
		long start = new Date().getTime();
		List<Plan> allPlans = findAllPlans();
		int i = 0;
		bulkRequest = client.prepareBulk();
		for (Plan plan : allPlans) {
			i++;
			plan.setCustomerId(plan.getOrgId());
			addToBulk(plan);
			if (i == 500) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		bulkRequest.execute().actionGet();
		long end = new Date().getTime();
		logger.info("finished indexing plans. {} seconds", (end - start) / 1000);
	}

	private void addToBulk(Plan plan) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.AUCTION_INDEX, "plans",
				String.valueOf(plan.getPlanId()));
		req.setSource(gson.toJson(plan));
		bulkRequest.add(req);
	}
}
