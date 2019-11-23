package com.bas.auction.core.dao;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MessageDAO implements SqlAware {
	private final static Logger logger = LoggerFactory.getLogger(MessageDAO.class);
	private final Properties prop;
	private final DaoJdbcUtil daoutil;

	@Autowired
	public MessageDAO(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
		this.prop = new Properties();
		loadFile();
	}

	private void loadFile() {
		try {
			URL resource = MessageDAO.class.getResource("/properties/messages.properties");
			Path path = Paths.get(resource.toURI());
			logger.info("Read messages from {}", path);
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				prop.load(reader);
			}
		} catch (URISyntaxException | IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getSqlPath() {
		return "messages";
	}

	public String get(String key) {
		return prop.getProperty(key);
	}

	public String get(String key, Map<String, String> params) {
		String res = prop.getProperty(key);
		StrSubstitutor ss = new StrSubstitutor(params);
		return ss.replace(res);
	}

	public String getFromDb(String code, String lang) {
		return daoutil.queryScalar(this, "get", code, lang);
	}

	public String getFromDb(String code, String lang, Map<String, String> params) {
		String msg = getFromDb(code, lang);
		StrSubstitutor ss = new StrSubstitutor(params);
		return ss.replace(msg);
	}
}
