package com.bas.auction.lookup.loader.dao.impl;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.Date;
import java.util.regex.Pattern;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.lookup.dto.UnitOfMeasure;
import com.bas.auction.lookup.loader.dao.UomLoadDAO;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UomLoadDAOImpl extends AbstractLookupLoadDAO implements UomLoadDAO {
	private final Logger logger = LoggerFactory.getLogger(UomLoadDAOImpl.class);
	private final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	private final Pattern pat = Pattern.compile("^\\d+\t.+$", flags);

	@Autowired
	public UomLoadDAOImpl(DaoJdbcUtil daoutil) {
		super(daoutil, "uom");
	}

	@Override
	protected UnitOfMeasure next() {
		while (scanner.hasNext()) {
			String next = scanner.next();
			if (pat.matcher(next).matches()) {
				String[] fields = next.split("\t");
				if (fields.length == 2) {
					UnitOfMeasure uom = new UnitOfMeasure();
					uom.setCode(fields[0]);
					uom.setMeasure(fields[1]);
					return uom;
				}
			}
		}
		return null;
	}

	@Override
	public void indexAllUomEntries(Client client) throws IOException {
		logger.info("start indexing all uom");
		this.client = client;
		long start = new Date().getTime();
		index();
		long end = new Date().getTime();
		logger.info("end indexing all uom. {} seconds", (end - start) / 1000);
	}

	@Override
	@SpringTransactional
	public void storeAllUomEntries() throws IOException {
		logger.info("start storing all uom");
		long start = new Date().getTime();
		try {
			storeInDb();
		} catch (DataAccessException e) {
			Throwable cause = e.getMostSpecificCause();
			if (cause instanceof BatchUpdateException)
				cause = ((BatchUpdateException) cause).getNextException();
			logger.error("Error loading uom codes", cause);
			daoutil.rollback();
		}
		long end = new Date().getTime();
		logger.info("end storing all uom. {} seconds", (end - start) / 1000);
	}

}
