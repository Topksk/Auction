package com.bas.auction.lookup.loader.dao;

import java.io.IOException;

import org.elasticsearch.client.Client;

public interface CityLoader {
	void indexAllCities(Client client) throws IOException;
}
