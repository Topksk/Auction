package com.bas.auction.core.utils.validation;

import com.bas.auction.core.crypto.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

public class PasswordValidator {
	private final static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

	public enum Strength {
		NORMAL, SHORT, WEAK
	}

	public Strength isValid(String password) {
		if (password == null || password.length() < 6)
			return Strength.SHORT;
		URL resource = PasswordValidator.class.getResource("/properties/banned_passwords.properties");
		try {
			Path path = Paths.get(resource.toURI());
			try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				Properties prop = new Properties();
				prop.load(reader);
				String val = prop.getProperty("list");
				if (val != null) {
					String[] list = val.split(",");
					if (Arrays.asList(list).contains(password))
						return Strength.WEAK;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return Strength.NORMAL;
	}
}
