package com.bas.auction.bid.search.service.impl;

import com.bas.auction.bid.search.service.BidSearchService;
import com.bas.auction.lookup.dto.Lookup;
import com.bas.auction.lookup.dto.LookupResults;
import com.bas.auction.search.*;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class BidSearchServiceImpl implements BidSearchService {

    private final SearchService searchService;

    @Autowired
    public BidSearchServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public LookupResults quickSearchBids(Map<String, Object> query) {
        String[] includeFields = {"recid", "doc_number", "title", "title_kz", "bids"};
        if (!query.containsKey("open_date")) {
            Calendar cal = Calendar.getInstance();
            // restrict quick search by 1 year
            cal.add(Calendar.YEAR, -1);
            query.put("open_date", cal.getTime());
        }
        Long supplierId = (Long) query.get("bids.supplier_id");
        Map<String, CriteriaType> qf = new LinkedHashMap<>();
        qf.put("open_date", SimpleCriteriaType.RANGE_GTE);
        Map<String, CriteriaType> qf2 = new LinkedHashMap<>();
        qf2.put("bids.bid_id", SimpleCriteriaType.FILTER);
        qf2.put("bids.supplier_id", SimpleCriteriaType.FILTER);
        qf.put("bids", new NestedFilterType(qf2, query));
        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        LookupResults lookupResults = new LookupResults();
        lookupResults.setStatus("success");
        String title = String.valueOf(query.get("title"));
        List<Lookup> result = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(s -> mapToLookupList(s, supplierId, title))
                .flatMap(List::stream)
                .collect(toList());
        lookupResults.getItems().addAll(result);
        return lookupResults;
    }

    @SuppressWarnings("unchecked")
    private List<Lookup> mapToLookupList(Map<String, Object> source, Long supplierId, String title) {
        List<Map<String, Object>> bids = (List<Map<String, Object>>) source.get("bids");
        return bids.stream()
                .filter(bid -> Long.parseLong(bid.get("supplier_id").toString()) == supplierId)
                .map(bid -> mapToLookup(source, bid, title))
                .collect(toList());
    }

    private Lookup mapToLookup(Map<String, Object> source, Map<String, Object> bid, String title) {
        Lookup lookup = new Lookup();
        lookup.setId(String.valueOf(bid.get("bid_id")));
        if (String.valueOf(source.get("doc_number")).contains(title) ||
                String.valueOf(source.get("title")).toUpperCase().contains(title.toUpperCase())) {
            lookup.setText(String.valueOf(source.get("title")));
        } else if (String.valueOf(source.get("title_kz")).toUpperCase()
                .contains(title.toUpperCase())) {
            lookup.setText(String.valueOf(source.get("title_kz")));
        }
        return lookup;
    }

    @Override
    public List<Map<String, Object>> searchBids(Map<String, Object> query) {
        String[] includeFields = {"recid", "doc_number", "neg_status", "title", "neg_type", "open_date", "close_date",
                "bids"};
        if (query.containsKey("title") && query.get("title") != null) {
            query.put("description", query.get("title"));
        }
        if (query.containsKey("bid_id"))
            query.put("bids.bid_id", query.remove("bid_id"));
        Object bidStatuses = null;
        if (query.containsKey("bid_status")) {
            bidStatuses = query.remove("bid_status");
        }
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        initQueryOpenDate(qf, query);
        initQueryCloseDate(qf, query);

        Map<String, CriteriaType> qf2 = new LinkedHashMap<>();
        qf2.put("bids.bid_id", SimpleCriteriaType.FILTER);
        qf2.put("bids.supplier_id", SimpleCriteriaType.FILTER);
        qf.put("bids", new NestedFilterType(qf2, query));
        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            SearchHits ihits = sh.getInnerHits().get("bids");
            for (SearchHit innerSh : ihits.getHits()) {
                Map<String, Object> source2 = new HashMap<>(sh.getSource());
                source2.put("recid", innerSh.getSource().get("bid_id"));
                source2.put("neg_id", sh.getId());
                result.add(source2);
            }
        }
        // query for bid statuses
        if (!result.isEmpty()) {
            includeFields = new String[]{"recid", "bid_status"};
            qf = new LinkedHashMap<>();
            qf.put("recid", SimpleCriteriaType.FILTER);
            qf.put("bid_status", SimpleCriteriaType.FILTER);
            List<Object> ids = result.stream().map(id -> id.get("recid")).collect(toList());
            query = new LinkedHashMap<>();
            query.put("recid", ids);
            if (bidStatuses != null)
                query.put("bid_status", bidStatuses);
            response = searchService.search("bids", qf, query, includeFields);
            Iterator<Map<String, Object>> iterator = result.iterator();
            while (iterator.hasNext()) {
                boolean remove = true;
                Map<String, Object> current = iterator.next();
                long bidId = Long.parseLong(current.get("recid").toString());
                for (SearchHit sh : response.getHits().getHits()) {
                    Map<String, Object> source = sh.getSource();
                    long recid = Long.parseLong(source.get("recid").toString());
                    remove = !(recid == bidId);
                    if (!remove) {
                        current.put("bid_status", source.get("bid_status"));
                        break;
                    }
                }
                if (remove)
                    iterator.remove();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchBidLines(Map<String, Object> query) {
        String[] includeFields = {"recid", "doc_number", "neg_type", "open_date", "close_date", "bids"};
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        long supplierId = (long) query.get("bids.supplier_id");
        Map<String, CriteriaType> qf2 = new LinkedHashMap<>();
        qf2.put("bids.bid_id", SimpleCriteriaType.FILTER);
        qf2.put("bids.supplier_id", SimpleCriteriaType.FILTER);
        qf.put("bids", new NestedFilterType(qf2, query));
        if (query.containsKey("title") && query.get("title") != null) {
            Object title = query.remove("title");
            query.put("neg_lines.item_name_ru", title);
            query.put("neg_lines.item_short_desc_ru", title);
            qf2 = new LinkedHashMap<>();
            qf2.put("neg_lines.item_name_ru", SimpleCriteriaType.QUERY);
            qf2.put("neg_lines.item_short_desc_ru", SimpleCriteriaType.QUERY);
            String[] innerInclFields = {"line_num", "item_name_ru"};
            qf.put("neg_lines", new NestedQueryType(qf2, query, innerInclFields));
        }
        initQueryOpenDate(qf, query);
        initQueryCloseDate(qf, query);

        if (query.containsKey("bid_id"))
            query.put("bids.bid_id", query.remove("bid_id"));
        Object bidStatuses = null;
        if (query.containsKey("bid_status")) {
            bidStatuses = query.remove("bid_status");
        }
        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            String negId = sh.getSource().remove("recid").toString();
            SearchHits ihits = sh.getInnerHits().get("neg_lines");
            if (ihits == null)
                continue;
            List<Map<String, Object>> bids = (List<Map<String, Object>>) sh.getSource().get("bids");
            for (Map<String, Object> bid : bids) {
                if (Long.parseLong(bid.get("supplier_id").toString()) == supplierId) {
                    String bidId = bid.get("bid_id").toString();
                    for (SearchHit innerSh : ihits.getHits()) {
                        String lineNum = innerSh.getSource().get("line_num").toString();
                        Map<String, Object> source = new HashMap<>(sh.getSource());
                        source.put("recid", bidId + "_" + lineNum);
                        source.put("neg_id", negId);
                        source.put("bid_id", bidId);
                        source.putAll(innerSh.getSource());
                        result.add(source);
                    }
                }
            }
        }
        // query for bid statuses
        if (!result.isEmpty()) {
            includeFields = new String[]{"recid", "bid_status"};
            qf = new LinkedHashMap<>();
            qf.put("recid", SimpleCriteriaType.FILTER);
            qf.put("bid_status", SimpleCriteriaType.FILTER);
            Set<Object> ids = new HashSet<>();
            for (Map<String, Object> id : result) {
                ids.add(id.get("bid_id"));
            }
            query = new LinkedHashMap<>();
            query.put("recid", ids);
            if (bidStatuses != null)
                query.put("bid_status", bidStatuses);
            response = searchService.search("bids", qf, query, includeFields);
            Iterator<Map<String, Object>> iterator = result.iterator();
            while (iterator.hasNext()) {
                boolean remove = true;
                Map<String, Object> current = iterator.next();
                long bidId = Long.parseLong(current.get("bid_id").toString());
                for (SearchHit sh : response.getHits().getHits()) {
                    Map<String, Object> source = sh.getSource();
                    long recid = Long.parseLong(source.get("recid").toString());
                    remove = !(recid == bidId);
                    if (!remove) {
                        current.put("bid_status", source.get("bid_status"));
                        break;
                    }
                }
                if (remove)
                    iterator.remove();
            }
        }
        return result;
    }

    private void initQueryOpenDate(Map<String, CriteriaType> qf, Map<String, Object> query) {
        if (query.containsKey("open_date_from") && query.containsKey("open_date_to")) {
            qf.put("open_date", SimpleCriteriaType.RANGE);
            Object[] range = {query.remove("open_date_from"), query.remove("open_date_to")};
            query.put("open_date", range);
        } else if (query.containsKey("open_date_from")) {
            qf.put("open_date", SimpleCriteriaType.RANGE_GTE);
            query.put("open_date", query.remove("open_date_from"));
        } else if (query.containsKey("open_date_to")) {
            qf.put("open_date", SimpleCriteriaType.RANGE_LTE);
            query.put("open_date", query.remove("open_date_to"));
        }

        if (!query.containsKey("open_date")) {
            Calendar cal = Calendar.getInstance();
            // restrict quick search by 1 year
            cal.add(Calendar.YEAR, -1);
            query.put("open_date", cal.getTime());
        }
    }

    private void initQueryCloseDate(Map<String, CriteriaType> qf, Map<String, Object> query) {
        if (query.containsKey("close_date_from") && query.containsKey("close_date_to")) {
            qf.put("actual_close_date", SimpleCriteriaType.RANGE);
            Object[] range = {query.remove("close_date_from"), query.remove("close_date_to")};
            query.put("actual_close_date", range);
        } else if (query.containsKey("close_date_from")) {
            qf.put("actual_close_date", SimpleCriteriaType.RANGE_GTE);
            query.put("actual_close_date", query.remove("close_date_from"));
        } else if (query.containsKey("close_date_to")) {
            qf.put("actual_close_date", SimpleCriteriaType.RANGE_LTE);
            query.put("actual_close_date", query.remove("close_date_to"));
        }
    }

    private Map<String, CriteriaType> queryFilterFieldMapping() {
        Map<String, CriteriaType> queryFilterMapping = new LinkedHashMap<>();
        queryFilterMapping.put("doc_number", SimpleCriteriaType.FILTER);
        queryFilterMapping.put("open_date", SimpleCriteriaType.RANGE_GTE);
        queryFilterMapping.put("actual_close_date", SimpleCriteriaType.RANGE_LTE);
        queryFilterMapping.put("neg_type", SimpleCriteriaType.FILTER);
        return queryFilterMapping;
    }
}
