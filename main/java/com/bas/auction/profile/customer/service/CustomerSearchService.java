package com.bas.auction.profile.customer.service;


import java.util.List;
import java.util.Map;

public interface CustomerSearchService {
    List<Map<String, Object>> searchCustomers(Map<String, Object> query);
}
