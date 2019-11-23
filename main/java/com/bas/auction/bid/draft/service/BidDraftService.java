package com.bas.auction.bid.draft.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.core.spring.SpringTransactional;

import java.io.IOException;
import java.util.List;

public interface BidDraftService {
    Bid create(User user, Long negId, String currencyCode);

    @SpringTransactional
    void updateWithoutIndexing(User user, Bid bid);

    Bid update(User user, Bid bid);

    Bid updateCurrency(User user, Long bidId, String currency);

    void delete(User user, Long bidId) throws IOException;

    Bid deleteFiles(User user, Long bidId, List<Long> ids) throws IOException;
}
