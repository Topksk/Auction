package com.bas.auction.profile.customer.dao;

import org.elasticsearch.client.Client;

public interface CustomerLoadDAO {
	void indexAllCustomers(Client client);
}
