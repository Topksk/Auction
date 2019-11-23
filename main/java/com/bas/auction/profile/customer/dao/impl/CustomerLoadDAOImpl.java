package com.bas.auction.profile.customer.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.profile.customer.dao.CustomerLoadDAO;
import com.bas.auction.profile.customer.dto.Customer;
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
public class CustomerLoadDAOImpl implements CustomerLoadDAO, GenericDAO<Customer> {
	private final Logger logger = LoggerFactory.getLogger(CustomerLoadDAOImpl.class);
	private final DaoJdbcUtil daoutil;
	private final Gson gson;
	private Client client;
	private BulkRequestBuilder bulkRequest;

	@Autowired
	public CustomerLoadDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
		this.gson = Utils.getGsonForSearchIndex();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	@Override
	public Class<Customer> getEntityType() {
		return Customer.class;
	}

	private List<Customer> findAllCustomers() {
		return daoutil.query(this, "list_all_customers");
	}

	@Override
	public void indexAllCustomers(Client client) {
		logger.info("start indexing all customers");
		this.client = client;
		long start = new Date().getTime();
		List<Customer> allCustomers = findAllCustomers();
		int i = 0;
		bulkRequest = client.prepareBulk();
		for (Customer customer : allCustomers) {
			i++;
			addToBulk(customer);
			if (i == 500) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		bulkRequest.execute().actionGet();
		long end = new Date().getTime();
		logger.info("finished indexing customers. {} seconds", (end - start) / 1000);
	}

	private void addToBulk(Customer customer) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.AUCTION_INDEX, "customers",
				String.valueOf(customer.getCustomerId()));
		req.setSource(gson.toJson(customer));
		bulkRequest.add(req);
	}
}
