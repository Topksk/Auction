package com.bas.auction.bid.search.service;


import com.bas.auction.lookup.dto.LookupResults;

import java.util.List;
import java.util.Map;

public interface BidSearchService {
    LookupResults quickSearchBids(Map<String, Object> query);

    List<Map<String, Object>> searchBids(Map<String, Object> query);

    List<Map<String, Object>> searchBidLines(Map<String, Object> query);
}
