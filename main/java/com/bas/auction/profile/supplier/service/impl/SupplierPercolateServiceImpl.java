package com.bas.auction.profile.supplier.service.impl;

import com.bas.auction.search.SearchService;
import com.bas.auction.profile.supplier.dto.NegNotification;
import com.bas.auction.profile.supplier.service.SupplierPercolateService;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.elasticsearch.index.query.FilterBuilders.nestedFilter;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class SupplierPercolateServiceImpl implements SupplierPercolateService {
    private final Logger logger = LoggerFactory.getLogger(SupplierPercolateServiceImpl.class);
    private final SearchService searchService;

    @Autowired
    public SupplierPercolateServiceImpl(SearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public void subscribe(long userId, NegNotification notification) {
        String[] category = notification.getCategory();
        QueryBuilder query = filteredQuery(
                boolQuery().must(termsQuery("category", category).minimumMatch(category.length)),
                nestedFilter("neg_lines", rangeQuery("neg_lines.amount_without_vat").gte(notification.getAmount())));

        searchService.indexAsync(".percolator", userId + "_" + notification.getNotificationId(), "query", query);
    }

    @Override
    public void deleteSubscribe(List<String> indexId) {
        searchService.bulkDeleteAsync(".percolator", indexId);
    }
}
