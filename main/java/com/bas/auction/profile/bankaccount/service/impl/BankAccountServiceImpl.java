package com.bas.auction.profile.bankaccount.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.profile.bankaccount.dao.BankAccountDAO;
import com.bas.auction.profile.bankaccount.dto.BankAccount;
import com.bas.auction.profile.bankaccount.service.BankAccountAlreadyExistsException;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.bankaccount.service.InvalidIbanException;
import com.bas.auction.profile.bankaccount.service.NoActiveMainBankAccountException;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    private final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);
    private final BankAccountDAO bankAccountDAO;
    private SupplierDAO supplierDAO;

    @Autowired
    public BankAccountServiceImpl(BankAccountDAO bankAccountDAO) {
        this.bankAccountDAO = bankAccountDAO;
    }

    @Autowired
    public void setSupplierDAO(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    @Override
    public List<BankAccount> findSupplierBankAccounts(Long supplierId) {
        return bankAccountDAO.findSupplierBankAccounts(supplierId);
    }

    @Override
    public List<BankAccount> findCustomerBankAccounts(Long customerId) {
        return bankAccountDAO.findCustomerBankAccounts(customerId);
    }

    @Override
    public boolean customerMainBankAccountExists(Long customerId) {
        return bankAccountDAO.customerMainBankAccountExists(customerId);
    }

    @Override
    public boolean supplierMainBankAccountExists(Long supplierId) {
        return bankAccountDAO.supplierMainBankAccountExists(supplierId);
    }

    @Override
    @SpringTransactional
    public BankAccount create(User user, BankAccount data) {
        if (!user.isMainUserOrSysadmin())
            throw new AccessDeniedException();
        validate(data);
        if (data.isMainAccount())
            bankAccountDAO.disableMainAccountFlag(user, data);
        data = bankAccountDAO.insert(user, data);
        return data;
    }

    @Override
    @SpringTransactional
    public BankAccount update(User user, BankAccount data) {
        if (!user.isMainUserOrSysadmin())
            throw new AccessDeniedException();
        validate(data);
        if (data.isMainAccount())
            bankAccountDAO.disableMainAccountFlag(user, data);
        data = bankAccountDAO.update(user, data);
        validateMainAccount(data);
        return data;
    }

    private void validate(BankAccount data) {
        if (data.isMainAccount() && !data.isActive()) {
            throw new NoActiveMainBankAccountException();
        }
        validateCustomerBankAccount(data);
        validateSupplierBankAccount(data);
    }

    private void validateCustomerBankAccount(BankAccount data) {
        if (data.getCustomerId() == null)
            return;
        if (!Validator.isValidIBAN(data.getAccount())) {
            throw new InvalidIbanException();
        }
        if (data.getAccountId() == 0) {
            boolean exist = bankAccountDAO.customerBankAccountExists(data.getCustomerId(), data.getAccount());
            if (exist)
                throw new BankAccountAlreadyExistsException();
        }
    }

    private void validateSupplierBankAccount(BankAccount data) {
        if (data.getSupplierId() == null)
            return;
        boolean nonresident = supplierDAO.findIsNonresident(data.getSupplierId());
        if (nonresident) {
            logger.debug("nonresident bank account");
        } else if (!Validator.isValidIBAN(data.getAccount())) {
            throw new InvalidIbanException();
        }
        if (data.getAccountId() == 0) {
            boolean exist = bankAccountDAO.supplierBankAccountExists(data.getSupplierId(), data.getAccount());
            if (exist)
                throw new BankAccountAlreadyExistsException();
        }
    }

    private void validateMainAccount(BankAccount data) {
        if (data.getCustomerId() != null && !customerMainBankAccountExists(data.getCustomerId())) {
            throw new NoActiveMainBankAccountException();
        }
        if (data.getSupplierId() != null && !supplierMainBankAccountExists(data.getSupplierId())) {
            throw new NoActiveMainBankAccountException();
        }
    }
}
