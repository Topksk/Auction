package com.bas.auction.profile.customer.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.billing.service.BillPlanService;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.customer.dto.Customer;
import com.bas.auction.profile.customer.service.CustomerService;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {
    private final static Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private final CustomerSettingDAO custSettingsDAO;
    private final CustomerDAO customerDAO;
    private final MessageDAO messages;
    private final DocFileDAO docFileDAO;
    private final Conf conf;
    private final BillPlanService billPlanService;
    private AddressService addressService;
    private BankAccountService bankAccountService;

    @Autowired
    public CustomerServiceImpl(CustomerSettingDAO custSettingsDAO, CustomerDAO customerDAO, MessageDAO messages, DocFileDAO docFileDAO, Conf conf, BillPlanService billPlanService) {
        this.custSettingsDAO = custSettingsDAO;
        this.customerDAO = customerDAO;
        this.messages = messages;
        this.docFileDAO = docFileDAO;
        this.conf = conf;
        this.billPlanService = billPlanService;
    }

    @Autowired
    public void setAddressDAO(AddressService addressDAO) {
        this.addressService = addressDAO;
    }

    @Autowired
    public void setBankAccountDAO(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Override
    public Customer create(User user, Customer customer) {
        logger.debug("create customer: bin={}", customer.getIdentificationNumber());
        customer.setRegStatus("IN_PROGRESS");
        customer = customerDAO.insert(user, customer);
        user = new User(1L);
        user.setCustomerId(customer.getCustomerId());
        billPlanService.createCustomerBillPlan("free", null, null, user);
        return customer;
    }

    @Override
    @SpringTransactional
    public Customer approveCustomer(Long userId, Long customerId) throws IOException {
        logger.debug("approve customer: {}", customerId);
        validateApprove(customerId);
        custSettingsDAO.validateCustomerMainSetting(customerId);
        Customer customer = customerDAO.updateRegStatus(userId, customerId, "AGREEMENT_NOT_SIGNED");
        insertUserAgreement(customer.getCustomerId());
        return customer;
    }

    @Override
    @SpringTransactional
    public Customer confirmRegistration(Long userId, Long customerId) {
        return customerDAO.updateRegStatus(userId, customerId, "APPROVED");
    }

    private void insertUserAgreement(long customerId) throws IOException {
        logger.debug("insert user agreement for customer: {}", customerId);
        Path file = Paths.get(conf.getUserAgreementFilePath());
        Map<String, String> attributes = new HashMap<>();
        attributes.put("customer_id", String.valueOf(customerId));
        attributes.put("file_type", "USER_AGREEMENT");
        String name = messages.get("AGREEMENT_FILE");
        docFileDAO.create(name, 1L, Boolean.TRUE, file, attributes);
    }

    private void validateApprove(Long customerId) {
        List<String> msgs = new ArrayList<>();
        if (!bankAccountService.customerMainBankAccountExists(customerId))
            msgs.add("MAIN_BANK_ACC_REQUIRED");
        if (!addressService.customerLegalAddressExists(customerId))
            msgs.add("LEGAL_ADDRESS_REQUIRED");
        if (!msgs.isEmpty())
            throw new ApplException(msgs);
    }
}
