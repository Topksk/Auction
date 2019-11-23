package com.bas.auction.profile.bankaccount.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.bankaccount.dto.BankAccount;

import java.util.List;

public interface BankAccountDAO {
	List<BankAccount> findSupplierBankAccounts(Long supplierId);

	List<BankAccount> findCustomerBankAccounts(Long customerId);

	boolean customerMainBankAccountExists(Long customerId);

	boolean supplierMainBankAccountExists(Long supplierId);

	BankAccount insert(User user, BankAccount data);

	BankAccount update(User user, BankAccount data);

	void disableMainAccountFlag(User user, BankAccount data);

	boolean customerBankAccountExists(Long customerId, String accountNumber);

	boolean supplierBankAccountExists(Long supplierId, String accountNumber);
}
