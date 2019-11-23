package com.bas.auction.search;

import com.bas.auction.core.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

import static org.elasticsearch.index.query.FilterBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class SearchService {
    public static final String MD = "md";
    public static final String AUCTION_SEARCH = "auction";
    public static final String AUCTION_INDEX = "auction_index";
    public static final String AUCTION_UPDATE = "auction_update";
    public static final int QUICK_SEARCH_RESULT_SIZE = 20;
    public static final int FULL_SEARCH_RESULT_SIZE = 200;
    private final Client client;
    private final static Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    public SearchService(Client client) {
        this.client = client;
    }

    public SearchResponse quickSearch(String type, QueryBuilder queryBuilder) {
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(MD)
                .setTypes(type)
                .setSize(QUICK_SEARCH_RESULT_SIZE)
                .setQuery(queryBuilder);
        return searchRequestBuilder.execute().actionGet();
    }

    public SearchResponse searchBit(String type, QueryBuilder queryBuilder) {
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(MD)
                .setTypes(type)
                .setSize(FULL_SEARCH_RESULT_SIZE)
                .setQuery(queryBuilder);
        return searchRequestBuilder.execute().actionGet();
    }

    public SearchResponse quickSearchCrossFields(String type, String query, String[] fields) {
        MultiMatchQueryBuilder multiMatchQuery = multiMatchQuery(query, fields)
                .type(Type.CROSS_FIELDS)
                .tieBreaker(.3f)
                .maxExpansions(QUICK_SEARCH_RESULT_SIZE);
        return quickSearch(type, multiMatchQuery);
    }

    public SearchResponse searchBitCrossFields(String type, String query, String[] fields) {
        MultiMatchQueryBuilder multiMatchQuery = multiMatchQuery(query, fields)
                .type(Type.CROSS_FIELDS)
                .tieBreaker(.3f)
                .maxExpansions(FULL_SEARCH_RESULT_SIZE);
        return searchBit(type, multiMatchQuery);
    }

    public SearchResponse quickSearchPhrasePrefix(String type, String query, String[] fields) {
        MultiMatchQueryBuilder multiMatchQuery = multiMatchQuery(query, fields)
                .type(Type.PHRASE_PREFIX)
                .slop(10)
                .maxExpansions(50);
        return quickSearch(type, multiMatchQuery);
    }

    public SearchResponse searchBitPhrasePrefix(String type, String query, String[] fields) {
        MultiMatchQueryBuilder multiMatchQuery = multiMatchQuery(query, fields)
                .type(Type.PHRASE_PREFIX)
                .slop(10)
                .maxExpansions(FULL_SEARCH_RESULT_SIZE);
        return searchBit(type, multiMatchQuery);
    }

    private FilterBuilder buildFilters(Map<String, CriteriaType> qf, Map<String, Object> query) {
        Iterator<Entry<String, Object>> iterator = query.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();
            if (value == null || value.toString().trim().isEmpty())
                iterator.remove();
        }
        FilterBuilder fb = null;
        if (!query.isEmpty()) {
            List<FilterBuilder> filterBuilderList = new ArrayList<>();
            for (Entry<String, CriteriaType> entry : qf.entrySet()) {
                String field = entry.getKey();
                CriteriaType criteriaType = entry.getValue();
                if (criteriaType instanceof SimpleCriteriaType) {
                    Object value = query.remove(field);
                    if (value == null)
                        continue;
                    else if (value instanceof String && ((String) value).trim().isEmpty())
                        continue;
                    Optional<FilterBuilder> simpleFilter = buildSimpleFilter((SimpleCriteriaType) criteriaType, field, value);
                    simpleFilter.ifPresent(filterBuilderList::add);
                } else if (criteriaType instanceof NestedFilterType) {
                    filterBuilderList.add(buildNestedFilter((NestedFilterType) criteriaType, field));
                }
            }
            FilterBuilder[] fbs = filterBuilderList.toArray(new FilterBuilder[filterBuilderList.size()]);
            fb = andFilter(fbs);
        }
        return fb;
    }

    private Optional<FilterBuilder> buildSimpleFilter(SimpleCriteriaType criteriaType, String field, Object value) {
        switch (criteriaType) {
            case FILTER:
                if (value instanceof String[])
                    return Optional.of(termsFilter(field, (String[]) value));
                else if (value instanceof Iterable)
                    return Optional.of(termsFilter(field, (Iterable<?>) value));
                else
                    return Optional.of(termFilter(field, value));
            case RANGE_LTE:
                return Optional.of(rangeFilter(field).lte(value).cache(false));
            case RANGE_GTE:
                return Optional.of(rangeFilter(field).gte(value).cache(false));
            case RANGE:
                Object[] range = (Object[]) value;
                return Optional.of(rangeFilter(field).gte(range[0]).lte(range[1]).cache(false));
            default:
        }
        return Optional.empty();
    }

    private FilterBuilder buildNestedFilter(NestedFilterType nestedFilterType, String field) {
        FilterBuilder filterBuilder = buildFilters(nestedFilterType.queryFieldMapping, nestedFilterType.query);
        QueryInnerHitBuilder queryInnerHitBuilder = new QueryInnerHitBuilder();
        queryInnerHitBuilder.setSize(FULL_SEARCH_RESULT_SIZE);
        return nestedFilter(field, filterBuilder).innerHit(queryInnerHitBuilder);
    }

    private QueryBuilder buildQuery(Map<String, CriteriaType> qf, Map<String, Object> query) {
        Iterator<Entry<String, Object>> iterator = query.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value == null || value.toString().trim().isEmpty())
                iterator.remove();
            else if (!qf.containsKey(key)) {
                qf.put(key, SimpleCriteriaType.QUERY);
            }
        }
        BoolQueryBuilder boolQueryBuilder = boolQuery();
        for (Entry<String, CriteriaType> entry : qf.entrySet()) {
            String field = entry.getKey();
            CriteriaType criteriaType = entry.getValue();
            QueryBuilder queryBuilder;
            if (criteriaType instanceof SimpleCriteriaType && criteriaType == SimpleCriteriaType.QUERY) {
                queryBuilder = matchPhrasePrefixQuery(field, query.get(field)).maxExpansions(QUICK_SEARCH_RESULT_SIZE);
            } else if (criteriaType instanceof NestedQueryType) {
                queryBuilder = buildNestedQuery((NestedQueryType) criteriaType, field);
            } else
                continue;
            boolQueryBuilder.should(queryBuilder);
        }
        return boolQueryBuilder;
    }

    private QueryBuilder buildNestedQuery(NestedQueryType nestedQueryType, String field) {
        QueryBuilder queryBuilder = buildQuery(nestedQueryType.qf, nestedQueryType.query);
        QueryInnerHitBuilder qib = new QueryInnerHitBuilder();
        qib.setSize(FULL_SEARCH_RESULT_SIZE);
        if (nestedQueryType.includeFields != null) {
            qib.setFetchSource(nestedQueryType.includeFields, null);
        }
        return nestedQuery(field, queryBuilder).innerHit(qib);
    }

    public SearchResponse search(String type, Map<String, CriteriaType> qf, Map<String, Object> query,
                                 String[] includeFields, Map<String, SortOrder> sortFields) {
        logger.debug("SearchService/search2");

        FilterBuilder filterBuilder = buildFilters(qf, query);
        logger.debug("ncp=1");
        QueryBuilder queryBuilder = buildQuery(qf, query);
        logger.debug("ncp=2");
        queryBuilder = filteredQuery(queryBuilder, filterBuilder);
        logger.debug("ncp=3");
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(AUCTION_SEARCH)
                .setTypes(type)
                // .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setSize(FULL_SEARCH_RESULT_SIZE).setQuery(queryBuilder);
        logger.debug("ncp=4");
        if (includeFields != null)
            searchRequestBuilder.setFetchSource(includeFields, null);
        logger.debug("ncp=5");
        if (sortFields != null) {
            for (Entry<String, SortOrder> field : sortFields.entrySet())
                searchRequestBuilder.addSort(field.getKey(), field.getValue());
        }
        logger.debug("ncp=6");
        return searchRequestBuilder.execute().actionGet();
    }

    public SearchResponse search(String type, Map<String, CriteriaType> qf, Map<String, Object> query,
                                 String[] includeFields) {

        logger.debug("SearchService/search1");

        return search(type, qf, query, includeFields, null);
    }

    public SearchResponse search(String type, Map<String, CriteriaType> qf, Map<String, Object> query) {
        return search(type, qf, query, null, null);
    }

    public ListenableActionFuture<IndexResponse> indexAsync(String type, Object id, Object obj) {
        return indexAsync(type, id.toString(), obj);
    }

    public ListenableActionFuture<IndexResponse> indexAsync(String type, String id, Object obj) {
        Gson gson = Utils.getGsonForSearchIndex();
        return client.prepareIndex(AUCTION_INDEX, type, id).setSource(gson.toJson(obj)).execute();
    }

    public ListenableActionFuture<IndexResponse> indexAsync(String type, String id, String field, Object obj) {
        return client.prepareIndex(AUCTION_INDEX, type, id).setSource(field, obj).execute();
    }

    public <T> ListenableActionFuture<IndexResponse> indexAsync(String type, String id, Map<String, T> data) {
        return client.prepareIndex(AUCTION_INDEX, type, id).setSource(data).execute();
    }

    public IndexResponse indexSync(String type, Object id, Object obj) {
        return indexAsync(type, id, obj).actionGet();
    }

    public <T> ListenableActionFuture<BulkResponse> bulkIndexAsync(String type, List<Map<String, T>> list) {
        Gson gson = Utils.getGsonForSearchIndex();
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        list.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .map(entry -> client.prepareIndex(AUCTION_INDEX, type, entry.getKey()).setSource(gson.toJson(entry.getValue())))
                .forEach(bulkRequestBuilder::add);
        return bulkRequestBuilder.execute();
    }

    public <T> ListenableActionFuture<UpdateResponse> updateAsync(String type, Object id, T data) {
        return updateAsync(type, id.toString(), data);
    }

    public <T> ListenableActionFuture<UpdateResponse> updateAsync(String type, String id, T data) {
        Gson gson = Utils.getGsonForSearchIndex();
        return client.prepareUpdate(AUCTION_UPDATE, type, id).setDoc(gson.toJson(data)).execute();
    }

    public <T> UpdateResponse updateSync(String type, Object id, T data) {
        return updateAsync(type, id, data).actionGet();
    }

    public <T> ListenableActionFuture<UpdateResponse> updateAsync(String type, String id, Map<String, T> data) {
        return client.prepareUpdate(AUCTION_UPDATE, type, id).setDoc(data).execute();
    }

    public <T> UpdateResponse updateSync(String type, Object id, Map<String, T> data) {
        return updateAsync(type, id.toString(), data).actionGet();
    }

    public ListenableActionFuture<DeleteResponse> deleteAsync(String type, Object id) {
        return deleteAsync(type, id.toString());
    }

    public ListenableActionFuture<DeleteResponse> deleteAsync(String type, String id) {
        return client.prepareDelete(AUCTION_UPDATE, type, id).execute();
    }

    public DeleteResponse deleteSync(String type, Object id) {
        return deleteAsync(type, id).actionGet();
    }

    public ListenableActionFuture<BulkResponse> bulkDeleteAsync(String type, List<String> ids) {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        ids.stream()
                .map(id -> client.prepareDelete(AUCTION_UPDATE, type, id))
                .forEach(bulkRequestBuilder::add);
        return bulkRequestBuilder.execute();
    }

    public BulkResponse bulkDeleteSync(String type, List<String> ids) {
        return bulkDeleteAsync(type, ids).actionGet();
    }

    public PercolateResponse percolate(String type, Object obj) {
        Gson gson = Utils.getGsonForSearchIndex();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("doc", gson.toJsonTree(obj));
        return client.preparePercolate()
                .setIndices(AUCTION_INDEX)
                .setDocumentType(type)
                .setSource(gson.toJson(jsonObject))
                .execute().actionGet();
    }
}
