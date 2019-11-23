package com.bas.auction.plans.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.plans.dto.Plan;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkResponse;

import java.util.List;

public interface PlanDAO {
	Plan findPlan(User user, Long planId);

	String findPlanStatus(Long planId);

	Plan insert(boolean index, Plan plan);

	Plan update(Plan plan);

	void approve(User user, List<Long> selectedPlans);

	void updateFailedNegPlansStatus(Long userId, Long negId);

	ListenableActionFuture<BulkResponse> indexNegPlans(Long negId);

	ListenableActionFuture<BulkResponse> indexPlansAsync(List<Plan> plans);

	void indexPlansSync(List<Plan> plans);

	void delete(List<Long> selectedPlans);

	void updatePublishedNegPlansStatus(Long userId, Long negId);

	void updateResumePublishedNegPlansStatus(Long userId, Long negId);

	void resetNegPlanAwardStatuses(Long userId, Long negId);
}
