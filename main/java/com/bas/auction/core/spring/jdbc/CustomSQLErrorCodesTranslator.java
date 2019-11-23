package com.bas.auction.core.spring.jdbc;

import com.bas.auction.core.dao.DaoException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import java.sql.BatchUpdateException;
import java.sql.SQLException;

public class CustomSQLErrorCodesTranslator extends SQLErrorCodeSQLExceptionTranslator {
	@Override
	protected DataAccessException customTranslate(String task, String sql, SQLException e) {
		if (e.getMessage().contains("persons_uq1")) {
			return new DaoException("IIN_EXISTS");
		}
		if (e.getMessage().contains("users_uq1")) {
			return new DaoException("LOGIN_EXISTS");
		}
		if (e.getMessage().contains("customers_uq1")) {
			return new DaoException("CUST_IDENTIFICATION_NUMBER_EXISTS");
		}
		if (e.getMessage().contains("suppliers_uq1")) {
			return new DaoException("SUPP_IDENTIFICATION_NUMBER_EXISTS");
		}
		if (e.getMessage().contains("plans_u1")) {
			return new DaoException("PLAN_NUMBER_EXISTS");
		}
		if (e.getMessage().contains("pd_bid_discounts_fk2")) {
			return new DaoException("DISCOUNT_USED_IN_BID");
		}
		if(e instanceof BatchUpdateException)
			return customTranslate(task, sql, e.getNextException());
		return null;
	}
}
