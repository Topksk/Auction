package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface EnstruLoadDAO {
	void indexAllEnstruEntries(Client client) throws IOException;

	void storeAllEnstruEntries() throws IOException;
}
