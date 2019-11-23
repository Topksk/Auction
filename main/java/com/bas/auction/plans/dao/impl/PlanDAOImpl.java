package com.bas.auction.plans.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.plans.dao.PlanDAO;
import com.bas.auction.plans.dto.Plan;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.bas.auction.search.SearchService;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

@Repository
public class PlanDAOImpl implements PlanDAO, GenericDAO<Plan> {
    private final Logger logger = LoggerFactory.getLogger(PlanDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final SearchService searchService;

    @Autowired
    public PlanDAOImpl(DaoJdbcUtil daoutil, SearchService searchService, CustomerSettingDAO customerSettingsDAO) {
        this.daoutil = daoutil;
        this.searchService = searchService;
    }

    @Override
    public String getSqlPath() {
        return "plans";
    }

    @Override
    public Class<Plan> getEntityType() {
        return Plan.class;
    }

    @Override
    public Plan findPlan(User user, Long planId) {
        logger.debug("get plan: {}", planId);
        Plan plan = daoutil.queryForObject(this, "get", planId, user.getCustomerId());
        if (plan != null) {
            plan.setCustomerId(plan.getOrgId());
        } else {
            logger.warn("plan not found: {}", planId);
        }
        return plan;
    }

    private List<Plan> findNegPlans(Long negId) {
        logger.debug("get neg plans: {}", negId);
        List<Plan> plans = daoutil.query(this, "get_neg_list", negId);
        plans.forEach(plan -> plan.setOrgId(plan.getOrgId()));
        return plans;
    }

    @Override
    public String findPlanStatus(Long planId) {
        return daoutil.queryScalar(this, "get_plan_status", planId);
    }

    private Object[] fieldVals(Plan plan) {
        return new Object[]{plan.getPlanNumber(), plan.getCustomerId(), plan.getFinancialYear(), plan.getItemCode(),
                plan.getItemCodeDesc(), plan.getItemNameRu(), plan.getItemNameKz(), plan.getItemShortDescRu(),
                plan.getItemShortDescKz(), plan.getItemLongDescRu(), plan.getItemLongDescKz(), plan.getPurchaseType(),
                plan.getPurchaseMethod(), plan.getKzContent(), plan.getPurchaseLocationKato(),
                plan.getPurchaseLocation(), plan.getShippingLocation(), plan.getIncoterms2010(), plan.getShippingDate(),
                plan.getPrepayment(), plan.getUomCode(), plan.getQuantity(), plan.getUnitPrice(), plan.getAmountWithoutVat(),
                plan.getAmountWithVat(), plan.getPurchasePriority(), plan.getNote(), plan.getItemCodeListType(),
                plan.getSettingId(), plan.getShippingRegion(), plan.getPurchasePeriod(), plan.getCreatedBy(),
                plan.getLastUpdatedBy()};
    }

    @Override
    @SpringTransactional
    public Plan insert(boolean index, Plan plan) {
        Object[] values = fieldVals(plan);
        KeyHolder kh = daoutil.insert(this, values);
        Long id = (Long) kh.getKeys().get("plan_id");
        plan.setPlanId(id);
        if (index)
            searchService.indexSync("plans", id, plan);
        return plan;
    }

    @Override
    @SpringTransactional
    public Plan update(Plan plan) {
        Object[] values = {plan.getPlanNumber(), plan.getFinancialYear(), plan.getItemCode(), plan.getItemCodeDesc(),
                plan.getItemNameRu(), plan.getItemNameKz(), plan.getItemShortDescRu(), plan.getItemShortDescKz(),
                plan.getItemLongDescRu(), plan.getItemLongDescKz(), plan.getPurchaseType(), plan.getPurchaseMethod(),
                plan.getKzContent(), plan.getPurchaseLocationKato(), plan.getPurchaseLocation(),
                plan.getShippingLocation(), plan.getIncoterms2010(), plan.getShippingDate(), plan.getPrepayment(),
                plan.getUomCode(), plan.getQuantity(), plan.getUnitPrice(), plan.getAmountWithoutVat(), plan.getAmountWithVat(),
                plan.getPurchasePriority(), plan.getNote(), plan.getShippingRegion(), plan.getPurchasePeriod(),
                plan.getLastUpdatedBy(), plan.getPlanId()};
        daoutil.update(this, values);
        searchService.indexSync("plans", plan.getPlanId(), plan);
        return plan;
    }

    @Override
    @SpringTransactional
    public void approve(User user, List<Long> selectedPlans) {
        List<Object[]> values = selectedPlans.stream()
                .map(id -> new Object[]{user.getUserId(), id})
                .collect(toList());
        daoutil.batchDML(this, "approve", values);
        for (Long id : selectedPlans) {
            Map<String, String> data = singletonMap("status", "APPROVED");
            searchService.updateAsync("plans", String.valueOf(id), data);
        }
    }

    @Override
    @SpringTransactional
    public void updatePublishedNegPlansStatus(Long userId, Long negId) {
        logger.debug("update published neg plans status: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "update_published_neg_plans_status", values);
    }

    @Override
    @SpringTransactional
    public void updateResumePublishedNegPlansStatus(Long userId, Long negId) {
        logger.debug("update resume published neg plans status: negId={}", negId);
        updateFailedNegPlansStatus(userId, negId);
        updateAwardedNegPlansStatus(userId, negId);
    }

    @Override
    public void updateFailedNegPlansStatus(Long userId, Long negId) {
        logger.debug("update failed neg plans status: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "update_failed_neg_plans_status", values);
    }

    private void updateAwardedNegPlansStatus(Long userId, Long negId) {
        logger.debug("update awarded neg plans status: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "update_awarded_neg_plans_status", values);
    }

    @Override
    public ListenableActionFuture<BulkResponse> indexNegPlans(Long negId) {
        return indexPlansAsync(findNegPlans(negId));
    }

    @Override
    public ListenableActionFuture<BulkResponse> indexPlansAsync(List<Plan> plans) {
        logger.debug("index plans");
        List<Map<String, Plan>> planList = plans.stream()
                .map(this::mapToMap)
                .collect(toList());
        if (!planList.isEmpty())
            return searchService.bulkIndexAsync("plans", planList);
        return null;
    }

    @Override
    public void indexPlansSync(List<Plan> plans) {
        if (!plans.isEmpty())
            indexPlansAsync(plans).actionGet();
    }

    private Map<String, Plan> mapToMap(Plan plan) {
        return singletonMap(String.valueOf(plan.getPlanId()), plan);
    }

    @Override
    @SpringTransactional
    public void delete(List<Long> selectedPlans) {
        List<Object[]> values = selectedPlans.stream()
                .map(this::mapToDeleteParams)
                .collect(toList());
        daoutil.batchDelete(this, values);
        List<String> selectedPlansStr = selectedPlans.stream()
                .map(String::valueOf)
                .collect(toList());
        searchService.bulkDeleteSync("plans", selectedPlansStr);
    }

    private Object[] mapToDeleteParams(Long planId) {
        return new Object[]{planId};
    }

    @Override
    public void resetNegPlanAwardStatuses(Long userId, Long negId) {
        logger.debug("reset neg plans award statuses: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "reset_neg_plans_award_statuses", values);
    }
}
