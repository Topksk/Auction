package com.bas.auction.plans.search.service;

import com.bas.auction.search.CriteriaType;
import com.bas.auction.search.SearchService;
import com.bas.auction.search.SimpleCriteriaType;
import com.bas.auction.plans.search.PlanSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class PlanSearchServiceImpl implements PlanSearchService {
    private final String[] includeFields = {"recid", "plan_number", "item_name_ru", "purchase_type", "quantity", "unit_price",
            "amount_without_vat", "purchase_method", "status"};
    private final SearchService searchService;

    @Autowired
    public PlanSearchServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public List<Map<String, Object>> searchPlans(Map<String, Object> query, boolean withUomDesc) {
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        Map<String, SortOrder> sf = new HashMap<>();
        sf.put("recid", SortOrder.DESC);

        SearchResponse response = searchService.search("plans", qf, query, includeFields, sf);
        SearchHit[] shs = response.getHits().getHits();
        Map<String, Object> uoms = null;
        if (withUomDesc)
            uoms = getUoms(shs);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : shs) {
            sh.getSource().put("recid", Long.parseLong(sh.getId()));
            if (withUomDesc) {
                String uomCode = (String) sh.getSource().get("uom_code");
                if (uomCode != null) {
                    String desc = (String) uoms.get(uomCode);
                    if (desc != null)
                        sh.getSource().put("uom_desc", desc);
                }
            }
            result.add(sh.getSource());
        }
        return result;
    }

    private Map<String, CriteriaType> queryFilterFieldMapping() {
        Map<String, CriteriaType> queryFilterFieldMapping = new LinkedHashMap<>();
        queryFilterFieldMapping.put("plan_number", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("customer_id", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("status", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("financial_year", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("item_code", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("purchase_method", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("purchase_type", SimpleCriteriaType.FILTER);
        return queryFilterFieldMapping;
    }

    private Map<String, Object> getUoms(SearchHit[] shs) {
        Map<String, CriteriaType> qf = new LinkedHashMap<>();
        qf.put("code", SimpleCriteriaType.FILTER);
        List<Object> codes = Arrays.stream(shs)
                .filter(sh -> sh.getSource().get("uom_code") != null)
                .map(sh -> sh.getSource().get("uom_code"))
                .collect(toList());
        Map<String, Object> query = new HashMap<>();
        query.put("code", codes);
        SearchResponse searchResponse = searchService.search("uom", qf, query);
        return Arrays.stream(searchResponse.getHits().getHits())
                .map(SearchHit::getSource)
                .map(this::mapUomCodeToMeasure)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<String, Object> mapUomCodeToMeasure(Map<String, Object> map) {
        return new AbstractMap.SimpleEntry<>((String) map.get("code"), map.get("measure"));
    }
}
