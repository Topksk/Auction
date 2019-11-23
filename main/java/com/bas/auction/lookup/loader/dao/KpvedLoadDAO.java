package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface KpvedLoadDAO {
	void indexAllKpvedEntries(Client client) throws IOException;

	void storeAllKpvedEntries() throws IOException;
}
