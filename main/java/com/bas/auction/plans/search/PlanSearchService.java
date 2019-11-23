package com.bas.auction.plans.search;


import java.util.List;
import java.util.Map;

public interface PlanSearchService {
    List<Map<String, Object>> searchPlans(Map<String, Object> query, boolean withUomDesc);
}
