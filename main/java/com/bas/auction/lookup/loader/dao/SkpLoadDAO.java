package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface SkpLoadDAO {
	void indexAllSkpEntries(Client client) throws IOException;

	void storeAllSkpEntries() throws IOException;
}
