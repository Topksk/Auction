package com.bas.auction.profile.bankaccount.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.bankaccount.dto.BankAccount;

import java.util.List;

public interface BankAccountService {
	List<BankAccount> findSupplierBankAccounts(Long supplierId);

	List<BankAccount> findCustomerBankAccounts(Long customerId);

	boolean customerMainBankAccountExists(Long customerId);

	boolean supplierMainBankAccountExists(Long supplierId);

	BankAccount create(User user, BankAccount data);

	BankAccount update(User user, BankAccount data);
}
