package com.bas.auction.profile.bankaccount.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.profile.bankaccount.dao.BankAccountDAO;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BankAccountDAOImpl implements BankAccountDAO, GenericDAO<BankAccount> {
	private final Logger logger = LoggerFactory.getLogger(BankAccountDAOImpl.class);
	private final DaoJdbcUtil daoutil;

	@Autowired
	public BankAccountDAOImpl(DaoJdbcUtil jdbcutil) {
		this.daoutil = jdbcutil;
	}

	@Override
	public String getSqlPath() {
		return "bank_accounts";
	}

	@Override
	public Class<BankAccount> getEntityType() {
		return BankAccount.class;
	}

	@Override
	public List<BankAccount> findSupplierBankAccounts(Long supplierId) {
		return daoutil.query(this, "get_supplier", supplierId);
	}

	@Override
	public List<BankAccount> findCustomerBankAccounts(Long customerId) {
		return daoutil.query(this, "get_customer", customerId);
	}

	@Override
	public boolean customerMainBankAccountExists(Long customerId) {
		logger.debug("check customer main bank account: {}", customerId);
		return daoutil.exists(this, "check_customer_main_bank_account", customerId);
	}

	@Override
	public boolean supplierMainBankAccountExists(Long supplierId) {
		logger.debug("check supplier main bank account: {}", supplierId);
		return daoutil.exists(this, "check_supplier_main_bank_account", supplierId);
	}

	@Override
	public boolean customerBankAccountExists(Long customerId, String accountNumber) {
		return daoutil.exists(this, "customer_acc_exists", customerId, accountNumber);
	}

	@Override
	public boolean supplierBankAccountExists(Long supplierId, String accountNumber) {
		return daoutil.exists(this, "supplier_acc_exists", supplierId, accountNumber);
	}

	@Override
	public void disableMainAccountFlag(User user, BankAccount data) {
		if (data.getCustomerId() != null) {
			Object[] params = { user.getUserId(), data.getCustomerId() };
			daoutil.dml(this, "switch_cust_main_acc", params);
		}
		if (data.getSupplierId() != null) {
			Object[] params = { user.getUserId(), data.getSupplierId() };
			daoutil.dml(this, "switch_supp_main_acc", params);
		}
	}

	@Override
	public BankAccount insert(User user, BankAccount data) {
		Object[] values = { data.getAccount(), data.getName(), data.getCurrency(), data.isMainAccount(),
				data.getSupplierId(), data.getCustomerId(), data.getBankNumber(), data.isActive(), user.getUserId(),
				user.getUserId() };
		KeyHolder kh = daoutil.insert(this, values);
		data.setAccountId((long) kh.getKeys().get("account_id"));
		return data;
	}

	@Override
	public BankAccount update(User user, BankAccount data) {
		Object[] values = { data.getAccount(), data.getName(), data.getCurrency(), data.isMainAccount(),
				data.getSupplierId(), data.getCustomerId(), data.getBankNumber(), data.isActive(), user.getUserId(),
				data.getAccountId() };
		daoutil.update(this, values);
		return data;
	}
}
