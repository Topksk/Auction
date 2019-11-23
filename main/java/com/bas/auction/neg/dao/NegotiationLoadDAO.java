package com.bas.auction.neg.dao;

import org.elasticsearch.client.Client;

public interface NegotiationLoadDAO {
	void indexAllNegs(Client client);
}
