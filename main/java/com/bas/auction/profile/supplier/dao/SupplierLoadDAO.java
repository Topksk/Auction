package com.bas.auction.profile.supplier.dao;

import org.elasticsearch.client.Client;

public interface SupplierLoadDAO {
	void indexAllSuppliers(Client client);
}
