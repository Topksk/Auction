package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface KatoLoadDAO {
	void indexAllKatoEntries(Client client) throws IOException;

	void storeAllKatoEntries() throws IOException;
}
