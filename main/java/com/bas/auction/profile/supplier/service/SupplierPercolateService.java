package com.bas.auction.profile.supplier.service;

import com.bas.auction.profile.supplier.dto.NegNotification;

import java.util.List;

/**
 * Created by bayangali.nauryz on 29.12.2015.
 */
public interface SupplierPercolateService {
    void subscribe(long userId, NegNotification notification);

    void deleteSubscribe(List<String> indexId);
}
