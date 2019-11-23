package com.bas.auction.plans.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.plans.dto.Plan;

import java.util.List;

public interface PlanService {
    Plan createForImport(User user, Plan plan);

    Plan create(User user, Plan plan);

    Plan update(User user, Plan plan);

    Plan approve(User user, List<Long> selectedPlans);

    void delete(List<Long> selectedPlans);
}
