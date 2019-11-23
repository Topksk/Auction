package com.bas.auction.bid.auction.service;

import java.math.BigDecimal;
import java.util.Map;

public interface AuctionBidService {
    Map<Integer, BigDecimal> calculateAuctionPrices(Long bidId);

    BigDecimal calculateAuctionPrice(Long bidId, Integer lineNum);
}
