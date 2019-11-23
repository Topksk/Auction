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
import com.bas.auction.lookup.dto.SKP;
import com.bas.auction.lookup.loader.dao.SkpLoadDAO;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SkpLoadDAOImpl extends AbstractLookupLoadDAO implements SkpLoadDAO {
	private final Logger logger = LoggerFactory.getLogger(SkpLoadDAOImpl.class);
	private final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	private final Pattern pat = Pattern.compile("^[\\d\\d\\.]+\\d{3} (A|B)\t.+$", flags);

	@Autowired
	public SkpLoadDAOImpl(DaoJdbcUtil daoutil) {
		super(daoutil, "skp");
	}

	@Override
	protected SKP next() {
		while (scanner.hasNext()) {
			String next = scanner.next().replaceAll("\n", " ").replaceAll("\r", " ");
			if (pat.matcher(next).matches()) {
				String[] fields = next.split("\t");
				if (fields.length == 2) {
					SKP skp = new SKP();
					skp.setCode(fields[0]);
					skp.setDescription(fields[1]);
					return skp;
				}
			}
		}
		return null;
	}

	@Override
	public void indexAllSkpEntries(Client client) throws IOException {
		logger.info("start indexing all skp");
		this.client = client;
		long start = new Date().getTime();
		index();
		long end = new Date().getTime();
		logger.info("end indexing all skp. {} seconds", (end - start) / 1000);
	}

	@Override
	@SpringTransactional
	public void storeAllSkpEntries() throws IOException {
		logger.info("start storing all skp");
		long start = new Date().getTime();
		try {
			storeInDb();
		} catch (DataAccessException e) {
			Throwable cause = e.getMostSpecificCause();
			if (cause instanceof BatchUpdateException)
				cause = ((BatchUpdateException) cause).getNextException();
			logger.error("Error loading skp codes", cause);
			daoutil.rollback();
		}
		long end = new Date().getTime();
		logger.info("end storing all skp. {} seconds", (end - start) / 1000);
	}

}
