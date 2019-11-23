package com.bas.auction.search;

import java.util.LinkedHashMap;
import java.util.Map;


public class NestedFilterType implements CriteriaType {
    final Map<String, CriteriaType> queryFieldMapping;
    final Map<String, Object> query;

    public NestedFilterType(Map<String, CriteriaType> qf, Map<String, Object> query) {
        this.queryFieldMapping = qf;
        this.query = new LinkedHashMap<>(qf.size());
        for (String key : qf.keySet()) {
            Object value = query.remove(key);
            if (value != null)
                this.query.put(key, value);
        }
    }
}
