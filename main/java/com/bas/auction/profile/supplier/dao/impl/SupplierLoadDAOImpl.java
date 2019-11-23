package com.bas.auction.profile.supplier.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.profile.supplier.dao.SupplierLoadDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
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
public class SupplierLoadDAOImpl implements SupplierLoadDAO, GenericDAO<Supplier> {
	private final Logger logger = LoggerFactory.getLogger(SupplierLoadDAOImpl.class);
	private final DaoJdbcUtil daoutil;
	private final Gson gson;
	private Client client;
	private BulkRequestBuilder bulkRequest;

	@Autowired
	public SupplierLoadDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
		this.gson = Utils.getGsonForSearchIndex();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	@Override
	public Class<Supplier> getEntityType() {
		return Supplier.class;
	}

	private List<Supplier> findAllSuppliers() {
		return daoutil.query(this, "list_all_suppliers");
	}

	@Override
	public void indexAllSuppliers(Client client) {
		logger.info("start indexing all suppliers");
		this.client = client;
		long start = new Date().getTime();
		List<Supplier> allSuppliers = findAllSuppliers();
		int i = 0;
		bulkRequest = client.prepareBulk();
		for (Supplier supplier : allSuppliers) {
			i++;
			addToBulk(supplier);
			if (i == 500) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		bulkRequest.execute().actionGet();
		long end = new Date().getTime();
		logger.info("finished indexing suppliers. {} seconds", (end - start) / 1000);
	}

	private void addToBulk(Supplier supplier) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.AUCTION_INDEX, "suppliers",
				String.valueOf(supplier.getSupplierId()));
		req.setSource(gson.toJson(supplier));
		bulkRequest.add(req);
	}

}
