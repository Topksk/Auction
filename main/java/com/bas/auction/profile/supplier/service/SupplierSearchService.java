package com.bas.auction.profile.supplier.service;

import java.util.List;
import java.util.Map;

public interface SupplierSearchService {
    List<Map<String, Object>> searchSuppliers(Map<String, Object> query);
}
