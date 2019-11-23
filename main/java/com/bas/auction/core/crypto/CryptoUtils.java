package com.bas.auction.core.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class CryptoUtils {
	private final static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
	private static final int BCRYPT_LOG2_ROUNDS = 12;
	private static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
			(byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
			(byte) 'e', (byte) 'f' };

	private String toHex(byte[] raw) {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, StandardCharsets.US_ASCII);
	}

	public String genStrongPasswdHash(String password) {
		String salt = BCrypt.gensalt(BCRYPT_LOG2_ROUNDS);
		return BCrypt.hashpw(password, salt);
	}

	public boolean checkPassword(String originalPassword, String storedPassword) {
		return BCrypt.checkpw(originalPassword, storedPassword);
	}

	private String getDigest(InputStream iStream, String digestAlg) throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[8192];
		MessageDigest digest = MessageDigest.getInstance(digestAlg);
		int count;
		while ((count = iStream.read(buffer)) > 0)
			digest.update(buffer, 0, count);
		return toHex(digest.digest());

	}

	public String getDigest(Path file) throws IOException {
		try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
			return getDigest(is, "SHA-512");
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String generateRandomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(64, random).toString(32);
	}
}
