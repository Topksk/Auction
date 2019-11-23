package com.bas.auction.core.dao;

import org.springframework.dao.DataAccessException;

public class DaoException extends DataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1048556050651617961L;
	private final String code;

	public DaoException(String code) {
		super(code);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
