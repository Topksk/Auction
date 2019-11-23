package com.bas.auction.bid.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface BidDAO {
    Bid findById(User user, Long bidId);

    Long findBidNegId(Long bidId);

    String findBidCurrency(Long bidId);

    List<Map<String, Long>> findNegBidIds(Long negId);

    List<Bid> findNegBids(User user, Long negId);

    List<Map<String, Object>> findLastDraftBids(Long supplierId);

    List<Map<String, Object>> findLastActiveBids(Long supplierId);

    List<Map<String, Object>> findLastAwardedBids(Long supplierId);

    List<Map<String, Object>> findLastRejectedBids(Long supplierId);

    List<Map<String, Object>> findPublishedTender2Stage2Negs(Long supplierId);

    List<Entry<Long, String>> findNegBidsCurrencyCodes(Long negId);

    List<Map<String, Object>> findNegAndBidLinesPrices(Long bidId);

    List<Map<String, Object>> findNegLinesUnitAndBidLinesMeanPrices(Long negId);

    Long findSupplierTender2Stage1PermittedBidId(Long supplierId, Long negId);

    Bid findAndIndexSync(User user, Long bidId);

    boolean findIsBidReplaced(Long bidId);

    Long findReplacedBidId(Long bidId);

    Long findReplacingBidId(Long bidId);

    Map<Integer, BigDecimal> findAuctionBidCurrentBestPrices(Long negId);

    BigDecimal findAuctionBidLineCurrentBestPrice(Long negId, Integer lineNum);

    Bid insert(User user, Bid bid);

    void updateBidForPublish(Long userId, Long bidId, BigDecimal rate);

    void delete(User user, Long bidId) throws IOException;

    Long copyBidForReplace(User user, Long sourceBidId);

    void update(User user, Bid bid);

    void updateCurrency(User user, Long bidId, String currency);

    void updateStatus(Long userId, Long bidId, String status);

    Map<Integer, BigDecimal> findSalesroomBestPrices(Long negId);

    Map<Long, Map<Integer, Long>> findAllSuppliersRanks(Long negId);

    Map<Integer, Long> findSupplierRanks(Long negId, Long supplierId);

    Map<Integer, Map<String, BigDecimal>> findSupplierLastSaleroomBids(Long negId, Long supplierId, String currencyCode);

    Map<Integer, BigDecimal> findAllSaleroomBids(Long negId, String currencyCode);

    void updateAllBidsUnlockExchangeRate(Long negId);

    void updateBidUnlockExchangeRate(Long bidId, BigDecimal exchangeRate);

    Map<Long, Date> findPublishDates(List<Long> bidIds);

    String findBidStatus(Long bidId);

    void updateNegActiveBidsAwardStatuses(Long userId, Long negId);

    void resetAwardStatuses(Long userId, Long negId);

    Long findSupplierActiveBidId(Long supplierId, Long negId);

    boolean activeBidExists(Long supplierId, Long negId);

    void updateBidExchangeRate(User user, Long bidId, BigDecimal exchangeRate);

    void indexSync(Bid bid);

    void reindexNegBidHeaders(Long negId);

    void deleteFromSearchIndexSync(Long bidId);

    boolean findIsDraft(Long bidId);
}
