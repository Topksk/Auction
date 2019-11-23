package com.bas.auction.plans.service;

import com.bas.auction.plans.dto.Plan;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;

import java.util.List;

public interface PlanValidationService {
    void validate(Plan plan, CustomerSetting customerSetting);

    void validateApprove(List<Long> selectedPlans);

    void validateDelete(List<Long> selectedPlans);
}
