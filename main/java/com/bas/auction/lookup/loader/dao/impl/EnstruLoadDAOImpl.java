package com.bas.auction.lookup.loader.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.lookup.dto.ENSTRU;
import com.bas.auction.lookup.loader.dao.EnstruLoadDAO;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.Date;
import java.util.regex.Pattern;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EnstruLoadDAOImpl extends AbstractLookupLoadDAO implements EnstruLoadDAO {
	private final Logger logger = LoggerFactory.getLogger(EnstruLoadDAOImpl.class);
	private final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
	private final Pattern pat = Pattern.compile("^[\\d\\d\\.]+\\d+\t.+$", flags);

	@Autowired
	public EnstruLoadDAOImpl(DaoJdbcUtil daoutil) {
		super(daoutil, "enstru");
	}

	protected ENSTRU next() {
		while (scanner.hasNext()) {
			String next = scanner.next().replaceAll("\n", " ").replaceAll("\r", " ");
			if (pat.matcher(next).matches()) {
				String[] fields = next.split("\t");
                if (fields.length >= 6) {
                    ENSTRU enstru = new ENSTRU();
					enstru.setCode(fields[0]);
					enstru.setShortDesc(fields[1]);
					enstru.setFullDesc(fields[2]);
					enstru.setType(fields[5]);
                    if (fields.length > 6)
                        enstru.setUom(fields[6]);
                    return enstru;
				}
			}
		}
		return null;
	}

	@Override
	public void indexAllEnstruEntries(Client client) throws IOException {
		logger.info("start indexing all enstru");
		this.client = client;
		long start = new Date().getTime();
		index();
		long end = new Date().getTime();
		logger.info("end indexing all enstru. {} seconds", (end - start) / 1000);
	}

	@Override
	@SpringTransactional
	public void storeAllEnstruEntries() throws IOException {
		logger.info("start storing all enstru");
		long start = new Date().getTime();
		try {
			storeInDb();
		} catch (DataAccessException e) {
			Throwable cause = e.getMostSpecificCause();
			if (cause instanceof BatchUpdateException)
				cause = ((BatchUpdateException) cause).getNextException();
			logger.error("Error loading enstru codes", cause);
			daoutil.rollback();
		}
		long end = new Date().getTime();
		logger.info("end storing all enstru. {} seconds", (end - start) / 1000);
	}

}
