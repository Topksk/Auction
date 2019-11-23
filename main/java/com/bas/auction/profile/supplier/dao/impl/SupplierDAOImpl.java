package com.bas.auction.profile.supplier.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserNotificationService;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SupplierDAOImpl implements SupplierDAO, GenericDAO<Supplier> {
    private final Logger logger = LoggerFactory.getLogger(SupplierDAOImpl.class);
    private final DocFileDAO docFileDAO;
    private final DaoJdbcUtil daoutil;
    private final SearchService searchService;
    private final UserNotificationService userNotifService;

    @Autowired
    public SupplierDAOImpl(DaoJdbcUtil daoutil, SearchService searchService, DocFileDAO docFileDAO, UserNotificationService userNotifService) {
        this.daoutil = daoutil;
        this.searchService = searchService;
        this.docFileDAO = docFileDAO;
        this.userNotifService = userNotifService;
    }

    @Override
    public String getSqlPath() {
        return "supplier";
    }

    @Override
    public Class<Supplier> getEntityType() {
        return Supplier.class;
    }

    @Override
    public String findBin(Long supplierId) {
        return daoutil.queryScalar(this, "get_bin", supplierId);
    }

    @Override
    public String findName(Long supplierId) {
        return daoutil.queryScalar(this, "get_name", supplierId);
    }

    @Override
    public Supplier findById(User user, Long supplierId) {
        logger.debug("get supplier: {}", supplierId);
        Supplier supplier = daoutil.queryForObject(this, "get", supplierId);
        if (user != null) {
            List<DocFile> list = docFileDAO.findByAttr(user, "supplier_id", supplierId);
            supplier.setRegFiles(list);
        }
        return supplier;
    }

    @Override
    public String findRegStatus(Long supplierId) {
        return daoutil.queryScalar(this, "get_reg_status", supplierId);
    }

    @Override
    public Supplier findBidSupplierById(Long supplierId) {
        logger.debug("get bid supplier: {}", supplierId);
        return daoutil.queryForObject(this, "get_bid_supplier", supplierId);
    }

    @Override
    public Long findIdByBin(String bin) {
        return daoutil.queryScalar(this, "get_id_by_bin", bin);
    }

    @Override
    public Supplier findUserOrg(User user) {
        Supplier supplier = daoutil.queryForObject(this, "user_supplier", user.getUserId());
        List<DocFile> list = docFileDAO.findByAttr(user, "supplier_id", supplier.getSupplierId());
        supplier.setRegFiles(list);
        return supplier;
    }

    @Override
    public Supplier findUserOrgByid(Long supplierId) {
        Supplier supplier = daoutil.queryForObject(this, "user_supplier_by_id", supplierId);
        List<DocFile> files = docFileDAO.findByAttr("supplier_id", String.valueOf(supplierId));
        supplier.setRegFiles(files);
        return supplier;
    }

    @Override
    public boolean exists(String bin) {
        return daoutil.exists(this, "supplier_exists", bin);
    }

    @Override
    public boolean findIsNonresident(Long supplierId) {
        return daoutil.queryScalar(this, "is_non_resident", supplierId);
    }

    @Override
    public boolean findIsIndividual(Long supplierId) {
        return daoutil.queryScalar(this, "is_individual", supplierId);
    }

    @Override
    public String findSupplierCountry(Long supplierId) {
        return daoutil.queryScalar(this, "country", supplierId);
    }

    @Override
    @SpringTransactional
    public Supplier insert(User user, Supplier supplier) {
        Object[] values = {supplier.isNonresident(), supplier.isLegalEntity(), supplier.getCountry(),
                supplier.getIdentificationNumber(), supplier.getBusinessEntityType(), supplier.getBusinessEntityTypeCustom(),
                supplier.getRnn(), supplier.getNameRu(), supplier.getNameKz(), supplier.getRegStatus(), user.getUserId(),
                user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        supplier.setSupplierId((long) kh.getKeys().get("supplier_id"));
        indexAsync(supplier);
        return supplier;
    }

    @Override
    @SpringTransactional
    public Supplier update(User user, Supplier supplier) {
        Object[] values = {supplier.getRnn(), supplier.getNameRu(), supplier.getNameKz(), supplier.getStateRegNumber(),
                supplier.getStateRegDate(), supplier.getStateRegDepartment(), supplier.getChiefFullName(),
                supplier.getChiefFullPosition(), supplier.getBusinessEntityType(), supplier.getBusinessEntityTypeCustom(),
                user.getUserId(), supplier.getSupplierId()};
        KeyHolder kh = daoutil.update(this, values);
        supplier.setRegStatus((String) kh.getKeys().get("reg_status"));
        indexAsync(supplier);
        return supplier;
    }

    @Override
    @SpringTransactional
    public Supplier updateRegStatus(User user, long id, String value) {
        logger.debug("update supplier reg status: supplierId = {}, status = {}", id, value);
        Object[] values = {value, user.getUserId(), id};
        daoutil.dml(this, "update_status", values);
        Supplier supplier = findById(null, id);
        indexAsync(supplier);
        return supplier;
    }

    private void indexAsync(Supplier supplier) {
        searchService.indexAsync("suppliers", String.valueOf(supplier.getSupplierId()), supplier);
    }

    @Override
    public List<Map<String, Object>> findForIntegraNotSent() {
        return daoutil.queryForMapList(this, "get_for_integra");
    }

    @Override
    @SpringTransactional
    public void setSent(Long supplierId) {
        Object[] values = {supplierId};
        daoutil.dml(this, "set_sent", values);
    }

    @Override
    public List<Long> findEmptyNotificationSetting() {
        return daoutil.queryScalarList(this, "get_empty_notification_setting");
    }

    @SpringTransactional
    public void insertFBStatus(String sqlCode, Object[] values) {
        try {
            daoutil.inserts(sqlCode, values);
            String userMail = "";
            List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", values[0]);
            for (int i = 0; i < s_res.size(); i++) {
                String mailCheck = "";
                mailCheck = s_res.get(i).get("text").toString();
                logger.info("mailCheck==="+mailCheck);
                if (Validator.isValidEmail(mailCheck)) {
                    if (userMail.equals(""))
                        userMail = mailCheck;
                    else
                        userMail = userMail+ ", " +mailCheck;
                }
            }
            Map<String, String> par_send = new HashMap<>();
            par_send.put("MSG_LANG", "RU");
            par_send.put("EMAIL",userMail);
            par_send.put("ORDER",values[1].toString());
            if (values[3].equals("APPROVED")) {
                String sqlCode1 = "f_transaction_pay";
                Object[] new_params = null;
                new_params = new Object[]{
                        values[0],
                        0,
                        values[1],
                        values[2],
                        "FILL"
                };
                daoutil.execStoredFunc2(sqlCode1, 4, new_params);
                par_send.put("MSG_CODE", "FB_PAY_APPROVED");
                userNotifService.sendOtherMessToAll(par_send);
            }
            else if (values[3].equals("DECLINED")) {
                par_send.put("MSG_CODE", "FB_PAY_DECLINED");
                userNotifService.sendOtherMessToAll(par_send);
            }
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            logger.info("insertFBStatus = "+e);
        }
    }
}
