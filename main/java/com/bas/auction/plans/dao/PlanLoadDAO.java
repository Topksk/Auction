package com.bas.auction.plans.dao;

import org.elasticsearch.client.Client;

public interface PlanLoadDAO {
	void indexAllPlans(Client client);
}
