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
import com.bas.auction.lookup.dto.KPVED;
import com.bas.auction.lookup.loader.dao.KpvedLoadDAO;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KpvedLoadDAOImpl extends AbstractLookupLoadDAO implements KpvedLoadDAO {
	private final Logger logger = LoggerFactory.getLogger(KpvedLoadDAOImpl.class);
	private final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	private final Pattern pat = Pattern.compile("^((\\d+(\\.\\d+)*)|(\\d\\w))\t.+$", flags);

	@Autowired
	public KpvedLoadDAOImpl(DaoJdbcUtil daoutil) {
		super(daoutil, "kpved");
	}

	protected KPVED next() {
		while (scanner.hasNext()) {
			String next = scanner.next().replaceAll("\n", " ").replaceAll("\r", " ");
			if (pat.matcher(next).matches()) {
				String[] fields = next.split("\t");
				if (fields.length == 3) {
					KPVED kpved = new KPVED();
					kpved.setCode(fields[0]);
					kpved.setDescription(fields[2]);
					return kpved;
				}
			}
		}
		return null;
	}

	@Override
	public void indexAllKpvedEntries(Client client) throws IOException {
		logger.info("start indexing all kpved");
		this.client = client;
		long start = new Date().getTime();
		index();
		long end = new Date().getTime();
		logger.info("end indexing all kpved. {} seconds", (end - start) / 1000);
	}

	@Override
	@SpringTransactional
	public void storeAllKpvedEntries() throws IOException {
		logger.info("start storing all kpved");
		long start = new Date().getTime();
		try {
			storeInDb();
		} catch (DataAccessException e) {
			Throwable cause = e.getMostSpecificCause();
			if (cause instanceof BatchUpdateException)
				cause = ((BatchUpdateException) cause).getNextException();
			logger.error("Error loading kpved codes", cause);
			daoutil.rollback();
		}
		long end = new Date().getTime();
		logger.info("end storing all kpved. {} seconds", (end - start) / 1000);
	}
}
