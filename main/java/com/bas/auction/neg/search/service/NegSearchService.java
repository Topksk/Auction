package com.bas.auction.neg.search.service;

import com.bas.auction.lookup.dto.LookupResults;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface NegSearchService {
    LookupResults quickSearchNegs(Map<String, Object> query);

    List<Map<String, Object>> searchNegs(Map<String, Object> query) throws ParseException;

    List<Map<String, Object>> searchNegLines(Map<String, Object> query) throws ParseException;

    List<Map<String, Object>> searchNegAdmin(Map<String, Object> query);
}
