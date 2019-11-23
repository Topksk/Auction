package com.bas.auction.bid.dao;

import org.elasticsearch.client.Client;

public interface BidLoadDAO {
	void indexAllBids(Client client);
}
