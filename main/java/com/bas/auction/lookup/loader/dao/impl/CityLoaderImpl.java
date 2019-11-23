package com.bas.auction.lookup.loader.dao.impl;

import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.lookup.dto.Lookup;
import com.bas.auction.lookup.loader.dao.CityLoader;
import com.google.gson.Gson;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CityLoaderImpl implements CityLoader {
	private final Logger logger = LoggerFactory.getLogger(CityLoaderImpl.class);
	protected final Gson gson;
	protected Client client;
	protected Scanner scanner;
	private BulkRequestBuilder bulkRequest;

	public CityLoaderImpl() {
		this.gson = Utils.getGsonForSearchIndex();
	}

	private File getFile() {
		return new File("tmp/cities.txt");
	}

	private BufferedReader getReaderFile() throws FileNotFoundException {
		FileInputStream fileInputStream = new FileInputStream(getFile());
		return new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
	}

	@Override
	public void indexAllCities(Client client) throws IOException {
		logger.info("start indexing cities");
		this.client = client;
		try (BufferedReader br = getReaderFile()) {
			String line;
			Set<String> cities = new HashSet<>();
			int i = 0;
			bulkRequest = client.prepareBulk();
			while ((line = br.readLine()) != null) {
				if (cities.contains(line))
					continue;
				else
					cities.add(line);
				i++;
				Lookup l = new Lookup();
				l.setId(i + "");
				l.setText(line);
				addToBulkMd(l);
			}
			bulkRequest.execute().actionGet();
		}
		logger.info("finished indexing cities");
	}

	protected void addToBulkMd(Lookup city) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.MD, "city", city.getId());
		req.setSource(gson.toJson(city));
		bulkRequest.add(req);
	}
}
