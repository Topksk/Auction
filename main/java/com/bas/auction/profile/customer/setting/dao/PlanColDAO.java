package com.bas.auction.profile.customer.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.plans.dto.PlanCol;

import java.util.List;

public interface PlanColDAO {
    List<PlanCol> findSettingPlanColList(Long settingId);

    List<PlanCol> findSettingDisplayedPlanColList(Long settingId);

    void createPlanCols(User user, Long settingId);

    int[] update(User user, Long settingId, List<PlanCol> cols);
}
