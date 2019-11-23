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
import com.bas.auction.lookup.dto.KATO;
import com.bas.auction.lookup.loader.dao.KatoLoadDAO;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class KatoLoadDAOImpl extends AbstractLookupLoadDAO implements KatoLoadDAO {
	private final Logger logger = LoggerFactory.getLogger(KatoLoadDAOImpl.class);
	private final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	private final Pattern pat = Pattern.compile("^\\d+\t\\d+.*$", flags);

	@Autowired
	public KatoLoadDAOImpl(DaoJdbcUtil daoutil) {
		super(daoutil, "kato");
	}

	protected KATO next() {
		while (scanner.hasNext()) {
			String next = scanner.next();
			if (pat.matcher(next).matches()) {
				String[] fields = next.split("\t");
				if (fields.length >= 5 && "RU".equals(fields[2])) {
					KATO k = new KATO();
					k.setCode(fields[1]);
					k.setRegion(fields[4]);
					if (fields.length > 5) {
						k.setCounty(fields[5]);
						if (fields.length > 6) {
							k.setDistrict(fields[6]);
							if (fields.length > 7)
								k.setTown(fields[7]);
						}
					}
					return k;
				}
			}
		}
		return null;
	}

	@Override
	public void indexAllKatoEntries(Client client) throws IOException {
		logger.info("start indexing all kato");
		this.client = client;
		long start = new Date().getTime();
		index();
		long end = new Date().getTime();
		logger.info("end indexing all kato. {} seconds", (end - start) / 1000);
	}

	@Override
	@SpringTransactional
	public void storeAllKatoEntries() throws IOException {
		logger.info("start storing all kato");
		long start = new Date().getTime();
		try {
			storeInDb();
		} catch (DataAccessException e) {
			Throwable cause = e.getMostSpecificCause();
			if (cause instanceof BatchUpdateException)
				cause = ((BatchUpdateException) cause).getNextException();
			logger.error("Error loading kato codes", cause);
			daoutil.rollback();
		}
		long end = new Date().getTime();
		logger.info("end storing all kato. {} seconds", (end - start) / 1000);
	}
}
