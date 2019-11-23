package com.bas.auction.profile.supplier.service.impl;

import com.bas.auction.search.CriteriaType;
import com.bas.auction.search.SearchService;
import com.bas.auction.search.SimpleCriteriaType;
import com.bas.auction.profile.supplier.service.SupplierSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierSearchServiceImpl implements SupplierSearchService {
    private final String[] includeFields = {"recid", "name_ru", "business_entity_type", "identification_number",
            "chief_full_name", "reg_status"};
    private final SearchService searchService;

    @Autowired
    public SupplierSearchServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public List<Map<String, Object>> searchSuppliers(Map<String, Object> query) {
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        SearchResponse response = searchService.search("suppliers", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            sh.getSource().put("recid", Long.parseLong(sh.getId()));
            result.add(sh.getSource());
        }
        return result;
    }

    private Map<String, CriteriaType> queryFilterFieldMapping() {
        Map<String, CriteriaType> queryFilterFieldMapping = new LinkedHashMap<>();
        queryFilterFieldMapping.put("identification_number", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("rnn", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("business_entity_type", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("reg_status", SimpleCriteriaType.FILTER);
        return queryFilterFieldMapping;
    }
}