package com.bas.auction.plans.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.plans.dao.PlanDAO;
import com.bas.auction.plans.dto.Plan;
import com.bas.auction.plans.service.PlanService;
import com.bas.auction.plans.service.PlanValidationService;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {
    private final Logger logger = LoggerFactory.getLogger(PlanServiceImpl.class);
    private final PlanDAO planDAO;
    private final CustomerSettingDAO customerSettingsDAO;
    private final PlanValidationService planValidationService;

    @Autowired
    public PlanServiceImpl(PlanDAO planDAO, CustomerSettingDAO customerSettingsDAO, PlanValidationService planValidationService) {
        this.planDAO = planDAO;
        this.customerSettingsDAO = customerSettingsDAO;
        this.planValidationService = planValidationService;
    }

    @Override
    public Plan createForImport(User user, Plan plan) {
        logger.trace("create plan for import");
        plan.setStatus("CREATED");
        BigDecimal amountWithoutVat = null;
        if (plan.getUnitPrice() != null && plan.getQuantity() != null)
            amountWithoutVat = plan.getUnitPrice().multiply(plan.getQuantity());
        plan.setAmountWithoutVat(amountWithoutVat);
        plan.setCreatedBy(user.getUserId());
        plan.setLastUpdatedBy(user.getUserId());
        return planDAO.insert(false, plan);
    }

    @Override
    public Plan create(User user, Plan plan) {
        logger.trace("create plan");
        CustomerSetting main = customerSettingsDAO.findMainWithoutDetails(user.getCustomerId());
        plan.setSettingId(main.getSettingId());
        plan.setStatus("CREATED");
        BigDecimal amountWithoutVat = null;
        if (plan.getUnitPrice() != null && plan.getQuantity() != null)
            amountWithoutVat = plan.getUnitPrice().multiply(plan.getQuantity());
        plan.setAmountWithoutVat(amountWithoutVat);
        plan.setItemCodeListType(main.getItemCodeListType());
        plan.setCreatedBy(user.getUserId());
        plan.setLastUpdatedBy(user.getUserId());
        planValidationService.validate(plan, main);
        return planDAO.insert(true, plan);
    }

    @Override
    public Plan update(User user, Plan plan) {
        logger.trace("update plan");
        BigDecimal amountWithoutVat = null;
        if (plan.getUnitPrice() != null && plan.getQuantity() != null)
            amountWithoutVat = plan.getUnitPrice().multiply(plan.getQuantity());
        plan.setAmountWithoutVat(amountWithoutVat);
        plan.setLastUpdatedBy(user.getUserId());
        CustomerSetting main = customerSettingsDAO.findMainWithoutDetails(user.getCustomerId());
        plan.setSettingId(main.getSettingId());
        plan.setItemCodeListType(main.getItemCodeListType());
        planValidationService.validate(plan, main);
        return planDAO.update(plan);
    }

    @Override
    public Plan approve(User user, List<Long> selectedPlans) {
        logger.debug("approve plans: {}", selectedPlans);
        planValidationService.validateApprove(selectedPlans);
        planDAO.approve(user, selectedPlans);
        if (selectedPlans.size() == 1)
            return planDAO.findPlan(user, selectedPlans.get(0));
        else
            return null;
    }

    @Override
    public void delete(List<Long> selectedPlans) {
        logger.debug("remove plans: {}", selectedPlans);
        planValidationService.validateDelete(selectedPlans);
        planDAO.delete(selectedPlans);
    }
}
