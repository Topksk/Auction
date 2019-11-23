package com.bas.auction.lookup.loader.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.SqlAware;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.lookup.dto.HasCode;
import com.bas.auction.lookup.dto.KATO;
import com.bas.auction.lookup.dto.UnitOfMeasure;
import com.google.gson.Gson;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public abstract class AbstractLookupLoadDAO implements SqlAware {
	protected final DaoJdbcUtil daoutil;
	protected final Gson gson;
	private String type;
	protected Client client;
	protected Scanner scanner;
	private BulkRequestBuilder bulkRequest;
	private static final int BATCH_THRESHOLD = 10000;

	public AbstractLookupLoadDAO(DaoJdbcUtil daoutil, String type) {
		this.daoutil = daoutil;
		this.type = type;
		this.gson = Utils.getGsonForSearchIndex();
	}

	private File getLookupFile() {
		String filePath = "tmp/" + type + ".txt";
		return new File(filePath);
	}

	protected void startScanner() throws FileNotFoundException {
		File file = getLookupFile();
		this.scanner = new Scanner(file, StandardCharsets.UTF_16.name());
		scanner.useDelimiter("\r\n");
	}

	protected void closeScanner() {
		this.scanner.close();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	protected abstract HasCode next();

	protected void index() throws IOException {
		startScanner();
		bulkRequest = client.prepareBulk();
		HasCode lookup;
		for (int i = 0; (lookup = next()) != null; i++) {
			addToBulkMd(lookup);
			if (i == BATCH_THRESHOLD) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		closeScanner();
		bulkRequest.execute().actionGet();
	}

	private Object[] getValues(HasCode lookup) {
		switch (type) {
		case "kato":
			KATO kato = (KATO) lookup;
			return new Object[] { kato.getCode(), kato.getRegion(), kato.getCounty(), kato.getDistrict(),
					kato.getTown() };
		case "uom":
			UnitOfMeasure uom = (UnitOfMeasure) lookup;
			return new Object[] { uom.getCode(), uom.getMeasure() };
		default:
			return new Object[] { lookup.getCode() };
		}
	}

	protected void storeInDb() throws IOException {
		startScanner();
		String sqlCode = "insert_" + type;
		List<Object[]> values = new ArrayList<>();
		HasCode lookup;
		for (int i = 0; (lookup = next()) != null; i++) {
			values.add(getValues(lookup));
			if (i == BATCH_THRESHOLD) {
				i = 0;
				daoutil.batchDML(this, sqlCode, values);
				values.clear();
			}
		}
		closeScanner();
		daoutil.batchDML(this, sqlCode, values);
	}

	protected void addToBulkMd(HasCode lookup) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.MD, type, lookup.getCode());
		req.setSource(gson.toJson(lookup));
		bulkRequest.add(req);
	}
}
