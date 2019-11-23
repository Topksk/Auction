package com.bas.auction.lookup.search.service;


import com.bas.auction.lookup.dto.LookupResults;

public interface LookupSearchService {
    LookupResults searchKato(String query);

    LookupResults searchEnstru(String query);

    LookupResults searchEnstruBit(String query);

    LookupResults searchSkp(String query);

    LookupResults searchSkpBit(String query);

    LookupResults searchKpved(String query);

    LookupResults searchUom(String query);

    LookupResults searchCity(String query);
}
