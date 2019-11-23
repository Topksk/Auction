package com.bas.auction.lookup.search.service.impl;

import com.bas.auction.lookup.dto.Lookup;
import com.bas.auction.lookup.dto.LookupResults;
import com.bas.auction.lookup.search.service.LookupSearchService;
import com.bas.auction.search.SearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class LookupSearchServiceImpl implements LookupSearchService {
    private final SearchService searchService;

    @Autowired
    public LookupSearchServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public LookupResults searchKato(String query) {
        String[] fields = {"code", "region", "county", "district", "town"};
        SearchResponse response = searchService.quickSearchPhrasePrefix("kato", query, fields);
        LookupResults lr = new LookupResults();
        lr.setStatus("success");
        List<Lookup> result = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(source -> this.mapToKatoLookup(source, fields))
                .collect(toList());
        lr.getItems().addAll(result);
        return lr;
    }

    private Lookup mapToKatoLookup(Map<String, Object> source, String[] fields) {
        Lookup lookup = new Lookup();
        lookup.setId(String.valueOf(source.get("code")));
        String description = Arrays.stream(fields)
                .filter(f -> !"code".equals(f))
                .map(source::get)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(v -> !v.isEmpty())
                .collect(joining(", "));
        lookup.setText(description);
        return lookup;
    }

    @Override
    public LookupResults searchEnstru(String query) {
        String[] fields = {"code", "short_desc", "full_desc"};
        SearchResponse response = searchService.quickSearchCrossFields("enstru", query, fields);
        return wrapSearchResults(response, "code", "short_desc", "full_desc");
    }

    @Override
    public LookupResults searchEnstruBit(String query) {
        String[] fields = {"code", "short_desc", "full_desc"};
        SearchResponse response = searchService.searchBitCrossFields("enstru", query, fields);
        return wrapSearchResults(response, "code", "short_desc", "full_desc");
    }

    @Override
    public LookupResults searchSkp(String query) {
        String[] fields = {"code", "description"};
        return quickSearch("skp", fields, "code", "description", query);
    }

    @Override
    public LookupResults searchSkpBit(String query) {
        String[] fields = {"code", "description"};
        return searchBit("skp", fields, "code", "description", query);
    }

    @Override
    public LookupResults searchKpved(String query) {
        String[] fields = {"code", "description"};
        return quickSearch("kpved", fields, "code", "description", query);
    }

    @Override
    public LookupResults searchUom(String query) {
        String[] fields = {"code", "measure"};
        return quickSearch("uom", fields, "code", "measure", query);
    }

    @Override
    public LookupResults searchCity(String query) {
        String[] fields = {"text"};
        return quickSearch("city", fields, "id", "text", query);
    }

    private LookupResults quickSearch(String type, String[] fields, String idField, String textField, String query) {
        SearchResponse response = searchService.quickSearchPhrasePrefix(type, query, fields);
        return wrapSearchResults(response, idField, textField, null);
    }

    private LookupResults searchBit(String type, String[] fields, String idField, String textField, String query) {
        SearchResponse response = searchService.searchBitPhrasePrefix(type, query, fields);
        return wrapSearchResults(response, idField, textField, null);
    }

    private LookupResults wrapSearchResults(SearchResponse response, String idField, String textField, String descriptionField) {
        LookupResults lr = new LookupResults();
        lr.setStatus("success");
        List<Lookup> result = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(source -> this.mapToLookup(source, idField, textField, descriptionField))
                .collect(toList());
        lr.getItems().addAll(result);
        return lr;
    }

    private Lookup mapToLookup(Map<String, Object> source, String idField, String textField, String descriptionField) {
        Lookup lookup = new Lookup();
        lookup.setId(String.valueOf(source.get(idField)));
        lookup.setText(String.valueOf(source.get(textField)));
        if (descriptionField != null)
            lookup.setDesc(String.valueOf(source.get(descriptionField)));
        return lookup;
    }


}
