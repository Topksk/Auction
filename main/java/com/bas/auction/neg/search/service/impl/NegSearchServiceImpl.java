package com.bas.auction.neg.search.service.impl;

import com.bas.auction.search.CriteriaType;
import com.bas.auction.search.NestedQueryType;
import com.bas.auction.search.SearchService;
import com.bas.auction.search.SimpleCriteriaType;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.lookup.dto.Lookup;
import com.bas.auction.lookup.dto.LookupResults;
import com.bas.auction.neg.search.service.NegSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class NegSearchServiceImpl implements NegSearchService {
    private final SearchService searchService;

    @Autowired
    public NegSearchServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public LookupResults quickSearchNegs(Map<String, Object> query) {
        String[] includeFields = {"recid", "doc_number", "title", "title_kz"};
        Map<String, CriteriaType> qf = new LinkedHashMap<>();
        qf.put("customer_id", SimpleCriteriaType.FILTER);
        qf.put("category", SimpleCriteriaType.FILTER);
        qf.put("neg_status", SimpleCriteriaType.FILTER);
        boolean notClosed = !query.containsKey("customer_id") && "PUBLISHED".equals(query.get("neg_status"));
        if (notClosed) {
            qf.put("open_date", SimpleCriteriaType.RANGE_LTE);
            qf.put("actual_close_date", SimpleCriteriaType.RANGE_GTE);
            query.put("open_date", new Date());
            query.put("actual_close_date", new Date());
        }
        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        LookupResults lr = new LookupResults();
        lr.setStatus("success");
        String searchString = String.valueOf(query.get("title"));
        List<Lookup> result = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(s -> mapToLookup(searchString, s))
                .collect(toList());
        lr.getItems().addAll(result);
        return lr;
    }

    private Lookup mapToLookup(String searchString, Map<String, Object> source) {
        Lookup lookup = new Lookup();
        lookup.setId(String.valueOf(source.get("recid")));
        String docNumber = String.valueOf(source.get("doc_number"));
        String text = null;
        if (docNumber.contains(searchString)
                || String.valueOf(source.get("title")).toUpperCase().contains(searchString.toUpperCase())) {
            text = String.valueOf(source.get("title"));
        } else if (String.valueOf(source.get("title_kz")).toUpperCase().contains(searchString.toUpperCase())) {
            text = String.valueOf(source.get("title_kz"));
        }
        if(text == null)
            text = String.valueOf(source.get("title"));
        lookup.setText(docNumber + " - " + text);
        return lookup;
    }

    @Override
    public List<Map<String, Object>> searchNegs(Map<String, Object> query) throws ParseException {
        String[] includeFields = {"recid", "doc_number", "neg_status", "title", "neg_type", "open_date",
                "close_date"};
        if (query.containsKey("title") && query.get("title") != null) {
            Object title = query.get("title");
            query.put("description", title);
        }
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        boolean onlyPublishedNegs = onlyPublishedNegs(query);
        if (onlyPublishedNegs)
            ensurePublishedNegDateRanges(query);
        initQueryOpenDate(qf, query);
        initQueryCloseDate(qf, query);

        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            Map<String, Object> source = sh.getSource();
            source.put("recid", Long.parseLong(sh.getId()));
            result.add(sh.getSource());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> searchNegLines(Map<String, Object> query) throws ParseException {
        String[] includeFields = {"recid", "doc_number", "neg_status", "neg_type", "open_date", "close_date"};
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        if (query.containsKey("title") && query.get("title") != null) {
            Object title = query.remove("title");
            query.put("neg_lines.item_name_ru", title);
            query.put("neg_lines.item_short_desc_ru", title);
            Map<String, CriteriaType> qf2 = new LinkedHashMap<>();
            qf2.put("neg_lines.item_name_ru", SimpleCriteriaType.QUERY);
            qf2.put("neg_lines.item_short_desc_ru", SimpleCriteriaType.QUERY);
            String[] innerInclFields = {"line_num", "item_name_ru"};
            qf.put("neg_lines", new NestedQueryType(qf2, query, innerInclFields));
        }
        boolean onlyPublishedNegs = onlyPublishedNegs(query);
        if (onlyPublishedNegs)
            ensurePublishedNegDateRanges(query);
        initQueryOpenDate(qf, query);
        initQueryCloseDate(qf, query);

        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            String negId = sh.getSource().remove("recid").toString();
            SearchHits ihits = sh.getInnerHits().get("neg_lines");
            if (ihits == null)
                continue;
            for (SearchHit innerSh : ihits.getHits()) {
                String lineNum = innerSh.getSource().get("line_num").toString();
                Map<String, Object> source = new HashMap<>(sh.getSource());
                source.put("neg_id", negId);
                source.put("recid", negId + "_" + lineNum);
                source.putAll(innerSh.getSource());

                result.add(source);
            }
        }
        return result;
    }

    private boolean onlyPublishedNegs(Map<String, Object> query) {
        return !query.containsKey("customer_id") && "PUBLISHED".equals(query.get("neg_status"));
    }

    private void initQueryOpenDate(Map<String, CriteriaType> qf, Map<String, Object> query) throws ParseException {
        Date now = new Date();
        boolean onlyPublishedNegs = onlyPublishedNegs(query);
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
        } else if (onlyPublishedNegs) {
            qf.put("open_date", SimpleCriteriaType.RANGE_LTE);
            query.put("open_date", now);
        }
    }

    private void initQueryCloseDate(Map<String, CriteriaType> qf, Map<String, Object> query) {
        Date now = new Date();
        boolean onlyPublishedNegs = onlyPublishedNegs(query);
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
        } else if (onlyPublishedNegs) {
            qf.put("actual_close_date", SimpleCriteriaType.RANGE_GTE);
            query.put("actual_close_date", now);
        }
    }

    private void ensurePublishedNegDateRanges(Map<String, Object> query) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.iso8601);
        Date now = new Date();
        for (Map.Entry<String, Object> entry : query.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() == null)
                continue;
            switch (key) {
                case "open_date_from":
                case "open_date_to":
                    String val = (String) entry.getValue();
                    Date date = sdf.parse(val);
                    if (date.compareTo(now) > 0)
                        entry.setValue(now);
                    break;
                case "close_date_from":
                case "close_date_to":
                    val = (String) entry.getValue();
                    date = sdf.parse(val);
                    if (date.compareTo(now) < 0)
                        entry.setValue(now);

                    break;
                default:
            }
        }
    }

    @Override
    public List<Map<String, Object>> searchNegAdmin(Map<String, Object> query) {
        String[] includeFields = {"recid", "doc_number", "neg_status", "title", "neg_type", "open_date", "close_date"};
        Map<String, CriteriaType> qf = queryFilterFieldMapping();
        SearchResponse response = searchService.search("negs", qf, query, includeFields);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit sh : response.getHits().getHits()) {
            sh.getSource().put("recid", Long.parseLong(sh.getId()));
            result.add(sh.getSource());
        }
        return result;
    }

    private Map<String, CriteriaType> queryFilterFieldMapping() {
        Map<String, CriteriaType> queryFilterFieldMapping = new LinkedHashMap<>();
        queryFilterFieldMapping.put("doc_number", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("customer_id", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("neg_status", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("category", SimpleCriteriaType.FILTER);
        queryFilterFieldMapping.put("neg_type", SimpleCriteriaType.FILTER);
        return queryFilterFieldMapping;
    }
}
