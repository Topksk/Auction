package com.bas.auction.profile.customer.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.customer.dto.Customer;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CustomerDAOImpl implements CustomerDAO, GenericDAO<Customer> {
    private final static Logger logger = LoggerFactory.getLogger(CustomerDAOImpl.class);
    private final DocFileDAO docFileDAO;
    private final DaoJdbcUtil daoutil;
    private final SearchService searchService;

    @Autowired
    public CustomerDAOImpl(DaoJdbcUtil daoutil, SearchService searchService, DocFileDAO docFileDAO,
                           CustomerSettingDAO custSettingsDAO) {
        this.daoutil = daoutil;
        this.searchService = searchService;
        this.docFileDAO = docFileDAO;
    }

    @Override
    public String getSqlPath() {
        return "customer";
    }

    @Override
    public Class<Customer> getEntityType() {
        return Customer.class;
    }

    @Override
    public Customer findUserOrg(User user) {
        logger.debug("get user customer: {}", user.getUserId());
        Customer customer = daoutil.queryForObject(this, "user_customer", user.getUserId());
        if (customer != null) {
            List<DocFile> list = docFileDAO.findByAttr(user, "customer_id", customer.getCustomerId());
            customer.setRegFiles(list);
        } else {
            logger.warn("user customer not found: {}", user.getUserId());
        }
        return customer;
    }

    @Override
    public String findRegStatus(Long customerId) {
        return daoutil.queryScalar(this, "get_reg_status", customerId);
    }

    @Override
    public String findBin(Long customerId) {
        return daoutil.queryScalar(this, "get_bin", customerId);
    }

    private Customer findById(long id) {
        return daoutil.queryForObject(this, "get", id);
    }

    @Override
    public Long findIdByBin(String bin) {
        return daoutil.queryScalar(this, "get_id_by_bin", bin);
    }

    @Override
    public Customer findByIdForUser(User user, Long customerId) {
        logger.debug("get customer: {}", customerId);
        Customer customer = findById(customerId);
        if (customer != null && user != null) {
            List<DocFile> list = docFileDAO.findByAttr(user, "customer_id", customer.getCustomerId());
            customer.setRegFiles(list);
        } else {
            logger.warn("customer not found: {}", customerId);
        }
        return customer;
    }

    @Override
    public boolean exists(String bin) {
        return daoutil.exists(this, "customer_exists", bin);
    }

    @Override
    @SpringTransactional
    public Customer insert(User user, Customer customer) {
        Object[] values = {customer.getIsOrganizer(), customer.getIdentificationNumber(), customer.getBusinessEntityType(),
                customer.getRnn(), customer.getNameRu(), customer.getNameKz(), customer.getStateRegNumber(), customer.getStateRegDate(),
                customer.getStateRegDepartment(), customer.getChiefFullName(), customer.getChiefFullPosition(), customer.getRegStatus(),
                customer.getHeadOrgIdentificationNumber(), customer.getHeadOrgNameRu(), customer.getHeadOrgNameKz(),
                user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        customer.setCustomerId((long) kh.getKeys().get("customer_id"));
        indexAsync(customer);
        return customer;
    }

    @Override
    @SpringTransactional
    public Customer update(User user, Customer customer) {
        Object[] values = {customer.getIsOrganizer(), customer.getIdentificationNumber(), customer.getBusinessEntityType(),
                customer.getRnn(), customer.getNameRu(), customer.getNameKz(), customer.getStateRegNumber(), customer.getStateRegDate(),
                customer.getStateRegDepartment(), customer.getChiefFullName(), customer.getChiefFullPosition(),
                customer.getHeadOrgIdentificationNumber(), customer.getHeadOrgNameRu(), customer.getHeadOrgNameKz(),
                user.getUserId(), customer.getCustomerId()};
        daoutil.update(this, values);
        indexAsync(customer);
        return customer;
    }

    @Override
    public Customer updateRegStatus(Long userId, Long customerId, String status) {
        Object[] params = {status, userId, customerId};
        daoutil.dml(this, "update_reg_status", params);
        Customer customer = findById(customerId);
        searchService.indexAsync("customers", customerId, customer);
        return customer;
    }

    @Override
    public Map<String, Object> findForIntegra(long customerId) {
        return daoutil.queryForMap(this, "get_for_integra", customerId);
    }

    @Override
    @SpringTransactional
    public void setSent(Long customerId) {
        Object[] values = {customerId};
        daoutil.dml(this, "set_sent", values);
    }

    private void indexAsync(Customer customer) {
        searchService.indexAsync("customers", String.valueOf(customer.getCustomerId()), customer);
    }
}