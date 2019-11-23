package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface UomLoadDAO {
	void indexAllUomEntries(Client client) throws IOException;

	void storeAllUomEntries() throws IOException;
}
