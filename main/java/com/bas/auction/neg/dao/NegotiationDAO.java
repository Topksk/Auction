package com.bas.auction.neg.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public interface NegotiationDAO {
    NegType findNegType(Long negId);

    boolean findIsAuction(Long negId);

    String findAuctionBidStepType(Long negId);

    Negotiation findNotDraftNeg(User user, Long negId);

    Negotiation findDumpingDataForPublishValidation(Long negId);

    Long findSettingId(Long negId);

    String findNegStatus(Long negId);

    boolean findIsPublishedNeg(Long negId);

    boolean findIsTender2Stage1(Long negId);

    boolean findIsTender2Stage2(Long negId);

    Long findParentNegId(Long negId);

    Negotiation findBidNeg(User user, Bid bid);

    Negotiation findCustomerNeg(User user, Long negId);

    Negotiation findAdminNeg(User user, Long negId);

    Negotiation findAdminNegHeader(Long negId);

    boolean findIsDraft(Long negId);

    Integer findNegStage(Long negId);

    List<Long> findUnlockList();

    List<Map<String, Object>> findLastPublishedNegs(Long customerId);

    List<Map<String, Object>> findLastDraftNegs(Long customerId);

    List<Map<String, Object>> findLastAwardedFailedNegs(Long customerId);

    Negotiation insert(Negotiation neg);

    List<Entry<Long, Long>> findAutoAwardList();

    void delete(Long negId);

    Negotiation update(User user, Negotiation neg);

    void reindexBidIds(Long negId);

    void updateStatus(Long userId, Long negId, String status);

    void updateActualCloseDate(Long userId, Long negId);

    void updateAwardDate(Long userId, Long negId);

    Negotiation findAndIndexSync(User user, Long negId);

    Negotiation findAndUpdateIndexAsync(User user, Long negId);

    Negotiation findAndUpdateIndexSync(User user, Long negId);

    void updateIndexAsync(Negotiation neg);

    void updateIndexSync(Negotiation neg);

    void deleteFromSearchIndex(Long negId);

    boolean awardedNegLineExists(Long negId);

    boolean notFailedNegLineExists(Long negId);

    boolean permittedTender2Stage1LineExists(Long negId);

    Long copyNeg(User user, Long sourceNegId, String docNumber, String status, Integer stage);

    BigDecimal findAuctionBidStep(Long negId);

    Double findSecondsLeftToClose(Long negId);

    Integer findExtendCount(Long negId);

    void updateForAuctionExtend(Long negId, Integer auctionExtDuration);

    List<Map<String, Object>> findNotSentNegs();

    void setSent(Long negId);

    Negotiation findForPublishValidation(Long negId);

    Negotiation findAuctionDataForPublishValidation(Long negId);

    void setNotificationAvaiable(Long negId);

    void setNotificationSent(Long negId);

    List<Negotiation> findNotificationAvailable();
}
