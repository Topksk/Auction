package com.bas.auction.plans.service.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.dao.SqlAware;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.plans.dao.PlanDAO;
import com.bas.auction.plans.dao.WrongPlanStatusForApproveException;
import com.bas.auction.plans.dao.WrongPlanStatusForDeleteException;
import com.bas.auction.plans.dto.Plan;
import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.plans.service.*;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanValidationServiceImpl implements PlanValidationService, SqlAware {
    private final static Logger logger = LoggerFactory.getLogger(PlanValidationServiceImpl.class);
    private final DaoJdbcUtil daoutil;
    private final PlanDAO planDAO;
    private final PlanColDAO planColDAO;
    private final Utils util;
    private final MessageDAO messages;

    @Autowired
    public PlanValidationServiceImpl(DaoJdbcUtil daoutil, PlanDAO planDAO, PlanColDAO planColDAO, Utils util, MessageDAO messages) {
        this.daoutil = daoutil;
        this.planDAO = planDAO;
        this.planColDAO = planColDAO;
        this.util = util;
        this.messages = messages;
    }

    @Override
    public String getSqlPath() {
        return "plans";
    }

    @Override
    public void validate(Plan plan, CustomerSetting customerSetting) {
        if (plan.getPlanId() > 0) {
            validateUpdate(plan);
        }
        Map<String, Method> planFields = findPlanProperties();
        List<PlanCol> planColList = planColDAO.findSettingPlanColList(plan.getSettingId());
        validateRequiredFields(plan, planColList, planFields);
        validateNumericFields(plan, planColList, planFields);
        validateTextFields(plan, planColList, customerSetting, planFields);
    }

    protected void validateUpdate(Plan plan) {
        String planStatus = planDAO.findPlanStatus(plan.getPlanId());
        boolean notCreatedStatus = isNotCreatedStatus(planStatus);
        if(notCreatedStatus)
            throw new WrongPlanStatusForUpdateException();
    }

    protected void validateRequiredFields(Plan plan, List<PlanCol> planColList, Map<String, Method> planFields) {
        planColList = planColList
                .stream()
                .filter(PlanCol::getDisplayInForm)
                .filter(PlanCol::getRequired)
                .collect(Collectors.toList());
        List<Map<String, String>> messageParams = new ArrayList<>();
        for (PlanCol planCol : planColList) {
            String colName = planCol.getColName();
            Object value = getFieldValue(planFields.get(colName), plan);
            if (planCol.getRequired() && isEmpty(value))
                messageParams.add(Collections.singletonMap("field_name", planCol.getDescription()));
        }
        if (messageParams.isEmpty())
            return;
        throw new PlanFieldRequiredException(messageParams);
    }

    protected void validateNumericFields(Plan plan, List<PlanCol> planColList, Map<String, Method> planFields) {
        planColList = planColList.stream()
                .filter(col -> "numeric".equals(col.getColType())
                        || "integer".equals(col.getColType())
                        || "bigint".equals(col.getColType()))
                .collect(Collectors.toList());
        for (PlanCol planCol : planColList) {
            String colName = planCol.getColName();
            Object value = getFieldValue(planFields.get(colName), plan);
            if (value == null)
                continue;
            switch (colName) {
                case "financial_year":
                    int year = ((Number) value).intValue();
                    if (year < getCurrentYear())
                        throw new PlanFinancialYearshouldNotBePastException();
                    break;
                case "kz_content":
                    int perc = ((Number) value).intValue();
                    if (perc < 0 || perc > 100)
                        throw new PlanKazContentNotInRangeException();
                    break;
                case "quantity":
                case "unit_price":
                case "amount_without_vat":
                    if (isGood(plan, planFields) && ((Number) value).doubleValue() < 0) {
                        throw new PlanFieldShouldBePositiveException(planCol.getDescription());
                    }
                    break;
            }
        }
    }

    private boolean isGood(Plan plan, Map<String, Method> planFields) {
        Object purchaseType = getFieldValue(planFields.get("purchase_type"), plan);
        return purchaseType != null && "GOOD".equals(purchaseType);
    }

    protected void validateTextFields(Plan plan, List<PlanCol> planColList, CustomerSetting set, Map<String, Method> planFields) {
        planColList = planColList.stream()
                .filter(col -> "text".equals(col.getColType()))
                .collect(Collectors.toList());
        Collection<String> purchaseMethods = getPurchaseMethods(set);
        Collection<String> purchaseTypes = getPurchaseTypes();
        Collection<String> incoterms2010 = getIncoterms2010();
        for (PlanCol planCol : planColList) {
            String colName = planCol.getColName();
            Object value = getFieldValue(planFields.get(colName), plan);
            if (value == null)
                continue;
            if (value.toString().length() > 2000)
                throw new PlanValueTooLongException(planCol.getDescription());
            switch (colName) {
                case "item_code":
                    if (!validItemCode(plan.getItemCodeListType(), value.toString())) {
                        throw new PlanItemCodeNotInListException();
                    }
                    break;
                case "purchase_method":
                    if (!purchaseMethods.contains(value.toString())) {
                        throw new PlanPurchaseMethodNotInListException();
                    }
                    break;
                case "uom_code":
                    if (!validUomCode(value.toString())) {
                        throw new PlanUomCodeNotInListException();
                    }
                    break;
                case "purchase_type":
                    if (!purchaseTypes.contains(value.toString())) {
                        throw new PlanPurchaseTypeNotInListException();
                    }
                    break;
                case "incoterms2010":
                    if (!incoterms2010.contains(value.toString())) {
                        throw new PlanIncoterms2010NotInListException();
                    }
                    break;
                case "shipping_region":
                    if (!validRegionCode(value.toString())) {
                        throw new PlanIncoterms2010NotInListException();
                    }
                    break;
                case "purchase_priority":
                    if (!validPurchasePriority(value.toString())) {
                        throw new PlanPurchasePriorityNotInListException();
                    }
                    break;
            }
        }
    }

    private boolean isEmpty(Object val) {
        return val == null || val instanceof String && ((String) val).trim().isEmpty();
    }

    public boolean validItemCode(String itemCodeListType, String code) {
        return daoutil.exists(this, "item_code_exists", itemCodeListType, code);
    }

    public boolean validUomCode(String code) {
        return daoutil.exists(this, "uom_code_exists", code);
    }

    public boolean validRegionCode(String code) {
        return daoutil.exists(this, "valid_region_code", code);
    }

    public boolean validPurchasePriority(String code) {
        return daoutil.exists(this, "valid_purchase_priority", code);
    }

    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private Collection<String> getPurchaseMethods(CustomerSetting set) {
        Set<String> purchaseMethods = new HashSet<>();
        if (set.isRfqEnabled())
            purchaseMethods.add("RFQ");
        if (set.isAuctionEnabled())
            purchaseMethods.add("AUCTION");
        if (set.isTenderEnabled())
            purchaseMethods.add("TENDER");
        if (set.isTender2Enabled())
            purchaseMethods.add("TENDER2");
        return purchaseMethods;
    }

    private Collection<String> getPurchaseTypes() {
        return Arrays.asList("GOOD", "WORK", "SERVICE");
    }

    private Collection<String> getIncoterms2010() {
        return Arrays.asList(messages.get("INCOTERMS2010").toUpperCase().split(","));
    }

    private Object getFieldValue(Method method, Plan plan) {
        try {
            return method.invoke(plan);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Method> findPlanProperties() {
        Map<String, Method> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Plan.class, Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                String name = util.camelCaseToUnderscore(pd.getName());
                map.put(name, pd.getReadMethod());
            }
        } catch (IntrospectionException e) {
            logger.error("Error importing plan", e);
        }
        return map;
    }

    @Override
    public void validateApprove(List<Long> selectedPlans) {
        boolean hasAnyNotDraftPlan = hasAnyNotDraftPlan(selectedPlans);
        if (hasAnyNotDraftPlan)
            throw new WrongPlanStatusForApproveException();
    }

    @Override
    public void validateDelete(List<Long> selectedPlans) {
        boolean hasAnyNotDraftPlan = hasAnyNotDraftPlan(selectedPlans);
        if (hasAnyNotDraftPlan)
            throw new WrongPlanStatusForDeleteException();
    }

    private boolean hasAnyNotDraftPlan(List<Long> selectedPlans) {
        return selectedPlans.stream()
                .map(planDAO::findPlanStatus)
                .anyMatch(this::isNotCreatedStatus);
    }

    private boolean isNotCreatedStatus(String status) {
        return !"CREATED".equals(status);
    }

}
