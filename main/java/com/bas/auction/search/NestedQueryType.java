package com.bas.auction.search;

import java.util.LinkedHashMap;
import java.util.Map;

public class NestedQueryType implements CriteriaType {
    final Map<String, CriteriaType> qf;
    final Map<String, Object> query;
    final String[] includeFields;

    public NestedQueryType(Map<String, CriteriaType> qf, Map<String, Object> query, String[] includeFields) {
        this.qf = qf;
        this.query = new LinkedHashMap<>(qf.size());
        this.includeFields = includeFields;
        for (String key : qf.keySet()) {
            Object value = query.remove(key);
            if (value != null)
                this.query.put(key, value);
        }
    }
}
