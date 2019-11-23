package com.bas.auction.neg.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.permission.dao.BidPermissionsDAO;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegTeamDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.NegLine;
import com.bas.auction.neg.dto.NegTeam;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class NegotiationDAOImpl implements NegotiationDAO, GenericDAO<Negotiation> {
    private final Logger logger = LoggerFactory.getLogger(NegotiationDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final SearchService searchService;
    private final NegTeamDAO negTeamDAO;
    private final NegLineDAO negLineDAO;
    private final NegFileService negFileService;
    private BidDAO bidDAO;
    private final BidPermissionsDAO bidPermsDAO;
    private final ExchangeRateService exchangeRateService;

    @Autowired
    public NegotiationDAOImpl(DaoJdbcUtil daoutil, SearchService searchService, NegTeamDAO negTeamDAO,
                              NegLineDAO negLineDAO,
                              NegFileService negFileService, BidPermissionsDAO bidPermsDAO, ExchangeRateService exchangeRateService) {
        this.daoutil = daoutil;
        this.searchService = searchService;
        this.negTeamDAO = negTeamDAO;
        this.negLineDAO = negLineDAO;
        this.negFileService = negFileService;
        this.bidPermsDAO = bidPermsDAO;
        this.exchangeRateService = exchangeRateService;
    }

    @Autowired
    private void setBidDAO(BidDAO bidDAO) {
        this.bidDAO = bidDAO;
    }

    @Override
    public String getSqlPath() {
        return "negotiations";
    }

    @Override
    public Class<Negotiation> getEntityType() {
        return Negotiation.class;
    }

    @Override
    public NegType findNegType(Long negId) {
        String res = daoutil.queryScalar(this, "get_neg_type", negId);
        if (res != null)
            return NegType.valueOf(res);
        return null;
    }

    @Override
    public boolean findIsAuction(Long negId) {
        return findNegType(negId) == Negotiation.NegType.AUCTION;
    }

    @Override
    public String findNegStatus(Long negId) {
        return daoutil.queryScalar(this, "get_neg_status", negId);
    }

    @Override
    public boolean findIsDraft(Long negId) {
        return daoutil.exists(this, "is_draft_neg", negId);
    }

    @Override
    public Integer findNegStage(Long negId) {
        return daoutil.queryScalar(this, "get_neg_stage", negId);
    }

    @Override
    public String findAuctionBidStepType(Long negId) {
        return daoutil.queryScalar(this, "get_auc_bid_step_type", negId);
    }

    @Override
    public BigDecimal findAuctionBidStep(Long negId) {
        return daoutil.queryScalar(this, "get_auc_bid_step", negId);
    }

    @Override
    public Double findSecondsLeftToClose(Long negId) {
        return daoutil.queryScalar(this, "get_seconds_left_to_close", negId);
    }

    @Override
    public Integer findExtendCount(Long negId) {
        return daoutil.queryScalar(this, "get_extend_count", negId);
    }

    @Override
    public void updateForAuctionExtend(Long negId, Integer auctionExtDuration) {
        Object[] values = {auctionExtDuration, auctionExtDuration, auctionExtDuration, negId};
        daoutil.dml(this, "update_for_auction_extend", values);
    }

    @Override
    public Negotiation findForPublishValidation(Long negId) {
        return daoutil.queryForObject(this, "get_for_publish_validation", negId);
    }

    @Override
    public Negotiation findAuctionDataForPublishValidation(Long negId) {
        return daoutil.queryForObject(this, "get_auction_data_for_publish_validation", negId);
    }

    @Override
    public Negotiation findDumpingDataForPublishValidation(Long negId) {
        return daoutil.queryForObject(this, "get_dumping_data_for_publish_validation", negId);
    }

    @Override
    public Long findSettingId(Long negId) {
        return daoutil.queryScalar(this, "get_neg_setting_id", negId);
    }

    private void setNegLines(Negotiation neg) {
        Long negId = neg.getNegId();
        List<NegLine> negLines = negLineDAO.findNegLines(negId);
        neg.setNegLines(negLines);
    }

    private void setBidNegLines(Negotiation neg, Long bidId) {
        Long negId = neg.getNegId();
        List<NegLine> negLines = negLineDAO.findBidNegLines(negId, bidId);
        neg.setNegLines(negLines);
    }

    private void setNegTeam(Negotiation neg) {
        Long negId = neg.getNegId();
        List<NegTeam> negTeam = negTeamDAO.findNegTeam(negId);
        neg.setNegTeam(negTeam);
    }

    private void setNegFiles(User user, Negotiation neg) {
        Long negId = neg.getNegId();
        List<DocFile> negFiles = negFileService.findNegFiles(user, negId);
        neg.setNegFiles(negFiles);
    }

    private void setNegLinesTeamAndFiles(User user, Negotiation neg) {
        setNegLines(neg);
        setNegTeam(neg);
        setNegFiles(user, neg);
    }

    private void setNegLinesAndFiles(User user, Negotiation neg) {
        setNegLines(neg);
        setNegFiles(user, neg);
    }

    private void setNegCustomerRule(Negotiation neg) {
        Long negCustomerRulesFileId = negFileService.findNegCustomerRulesFileId(neg.getCustomerId());
        neg.setCustomerRulesFileId(negCustomerRulesFileId);
    }

    @Override
    public Negotiation findNotDraftNeg(User user, Long negId) {
        logger.debug("get not draft neg: {}", negId);
        Negotiation neg = daoutil.queryForObject(this, "get_not_draft", negId);
        if (neg != null) {
            setNegLinesAndFiles(user, neg);
            setNegCustomerRule(neg);
        } else
            logger.warn("not draft neg not found: {}", negId);
        return neg;
    }

    @Override
    public boolean findIsPublishedNeg(Long negId) {
        return daoutil.exists(this, "is_published_neg", negId);
    }

    @Override
    public boolean findIsTender2Stage1(Long negId) {
        return daoutil.exists(this, "is_tender2_stage1", negId);
    }

    @Override
    public boolean findIsTender2Stage2(Long negId) {
        return daoutil.exists(this, "is_tender2_stage2", negId);
    }

    @Override
    public Long findParentNegId(Long negId) {
        return daoutil.queryScalar(this, "get_parent_neg_id", negId);
    }

    private void setAuctionBestPrices(Negotiation neg, String currencyCode) {
        Map<Integer, BigDecimal> bestPrices = bidDAO.findAuctionBidCurrentBestPrices(neg.getNegId());
        BigDecimal rate = exchangeRateService.findCurrentExchangeRate(currencyCode);
        neg.getNegLines().stream()
                .map(negLine -> negLine.setBestPrice(bestPrices.get(negLine.getLineNum())))
                .forEach(negLine ->
                        negLine.setBestPrice(negLine.getBestPrice().divide(rate, 2, RoundingMode.DOWN)));
    }

    @Override
    public Negotiation findBidNeg(User user, Bid bid) {
        logger.debug("get neg for bid: {}", bid.getBidId());
        Negotiation neg = daoutil.queryForObject(this, "get_bid_neg", bid.getBidId());
        if (neg != null) {
            setBidNegLines(neg, bid.getBidId());
            if (neg.isPublished())
                setAuctionBestPrices(neg, bid.getCurrencyCode());
            setNegCustomerRule(neg);
            setNegFiles(user, neg);
            convertNegLinesPricesToBidCurrencyRate(neg, bid);
        } else
            logger.warn("neg not found for bid: {}", bid.getBidId());
        return neg;
    }

    private void convertNegLinesPricesToBidCurrencyRate(Negotiation neg, Bid bid) {
        BigDecimal rate = bid.getUnlockExchangeRate();
        if (neg.getNegLines() != null) {
            neg.getNegLines().forEach(line -> {
                BigDecimal amount = line.getAmountWithoutVat().divide(rate, 2, RoundingMode.DOWN);
                line.setAmountWithoutVat(amount);
                BigDecimal unitPrice = line.getUnitPrice().divide(rate, 2, RoundingMode.DOWN);
                line.setUnitPrice(unitPrice);
            });
        }
    }

    private void setNegLinesBidPermissions(Negotiation neg) {
        Map<Integer, List<BidLinePermissions>> allBp =
                bidPermsDAO.findBidLinePermissions(neg.getNegId()).stream()
                        .collect(groupingBy(BidLinePermissions::getBidLineNum));
        neg.getNegLines().forEach(negLine -> negLine.setBidLinePermissions(allBp.get(negLine.getLineNum())));
    }

    @Override
    public Negotiation findCustomerNeg(User user, Long negId) {
        logger.debug("get customer neg: {}", negId);
        Negotiation neg = daoutil.queryForObject(this, "get_customer", negId, user.getCustomerId());
        if (neg != null) {
            setNegLinesTeamAndFiles(user, neg);
            setNegCustomerRule(neg);
            boolean voting = neg.isVoting() || neg.isVotingFinished();
            if (voting)
                setNegLinesBidPermissions(neg);
            if (voting || neg.isFailed() || neg.isAwarded() || neg.isFirstStageFinished()) {
                setNegBids(user, neg);
            }
        } else
            logger.warn("customer neg not found: {}", negId);
        return neg;
    }

    private void setNegBids(User user, Negotiation neg) {
        Long negId = neg.getNegId();
        List<Bid> bids = bidDAO.findNegBids(user, negId);
        neg.setBids(bids);
    }

    @Override
    public Negotiation findAdminNeg(User user, Long negId) {
        logger.debug("get neg for admin: {}", negId);
        Negotiation neg = findAdminNegHeader(negId);
        if (neg != null) {
            setNegLinesTeamAndFiles(user, neg);
            setNegCustomerRule(neg);
        } else
            logger.warn("neg for admin not found: {}", negId);
        return neg;
    }

    @Override
    public Negotiation findAdminNegHeader(Long negId) {
        return daoutil.queryForObject(this, "get_admin", negId);
    }

    @Override
    public boolean awardedNegLineExists(Long negId) {
        return daoutil.exists(this, "awarded_neg_line_exists", negId);
    }

    @Override
    public boolean notFailedNegLineExists(Long negId) {
        return daoutil.exists(this, "not_failed_neg_line_exists", negId);
    }

    @Override
    public boolean permittedTender2Stage1LineExists(Long negId) {
        return daoutil.exists(this, "permitted_tender2_stage1_line_exists", negId);
    }

    private List<Map<String, Object>> findLastNegsWithStatuses(Long customerId, String... statuses) {
        logger.debug("get last negs: customerId = {}, statuses = {}", customerId, statuses);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String status : statuses) {
            result.addAll(daoutil.queryForMapList(this, "get_last_negs_with_status", customerId, status));
        }
        result.sort((x, y) -> {
            Long negId1 = (Long) x.get("recid");
            Long negId2 = (Long) y.get("recid");
            return negId2.compareTo(negId1);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> findLastPublishedNegs(Long customerId) {
        return findLastNegsWithStatuses(customerId, "PUBLISHED", "VOTING", "VOTING_FINISHED");
    }

    @Override
    public List<Map<String, Object>> findLastDraftNegs(Long customerId) {
        return findLastNegsWithStatuses(customerId, "DRAFT");
    }

    @Override
    public List<Map<String, Object>> findLastAwardedFailedNegs(Long customerId) {
        return findLastNegsWithStatuses(customerId, "AWARDED", "NEG_FAILED", "FIRST_STAGE_FINISHED");
    }

    @Override
    public Negotiation insert(Negotiation neg) {
        Long id = daoutil.seqNextval("pd_neg_header_neg_id_seq");
        neg.setNegId(id);
        if (neg.getDocNumber() == null)
            neg.setDocNumber(id.toString());
        Object[] values = {neg.getNegId(), neg.getDocNumber(), neg.getCustomerId(), neg.getTitle(), neg.getTitleKz(),
                neg.getNegType().toString(), neg.getNegStatus(), neg.getSettingId(), neg.getStage(), neg.getCreatedBy(),
                neg.getLastUpdatedBy()};
        daoutil.insert(this, values);
        return neg;
    }

    @Override
    public Negotiation update(User user, Negotiation neg) {
        Long negId = neg.getNegId();
        neg.setActualCloseDate(neg.getCloseDate());
        Object[] values = {neg.getTitle(), neg.getTitleKz(), neg.getDescription(), neg.getCategory(),
                neg.getOpenDate(), neg.getCloseDate(), neg.getUnlockDate(), neg.getActualCloseDate(),
                neg.getMinBidLimitDays(), neg.isDumpingControlEnabled(), neg.getGoodDumpingCalcMethod(),
                neg.getGoodDumpingThreshold(), neg.getWorkDumpingCalcMethod(), neg.getWorkDumpingThreshold(),
                neg.getServiceDumpingCalcMethod(), neg.getServiceDumpingThreshold(), neg.getAuctionBidStep(),
                neg.getAuctionBidStepType(), user.getUserId(), negId, user.getCustomerId()};
        daoutil.update(this, values);
        return neg;
    }

    @Override
    @SpringTransactional
    public void delete(Long negId) {
        daoutil.delete(this, new Object[]{negId});
    }

    @Override
    @SpringTransactional
    public void updateStatus(Long userId, Long negId, String status) {
        Object[] values = {status, userId, negId};
        daoutil.dml(this, "update_status", values);
    }

    @Override
    @SpringTransactional
    public void updateActualCloseDate(Long userId, Long negId) {
        Object[] values = {userId, negId};
        daoutil.dml(this, "update_actual_close_date", values);
    }

    @Override
    @SpringTransactional
    public void updateAwardDate(Long userId, Long negId) {
        Object[] values = {userId, negId};
        daoutil.dml(this, "update_award_date", values);
    }

    @Override
    public Long copyNeg(User user, Long sourceNegId, String docNumber, String status, Integer stage) {
        Object[] values = {docNumber, status, stage, user.getUserId(), user.getUserId(), sourceNegId};
        KeyHolder keyHolder = daoutil.dml(this, "copy_neg", values);
        return (Long) keyHolder.getKeys().get("neg_id");
    }

    @Override
    public List<Entry<Long, Long>> findAutoAwardList() {
        List<Map<String, Object>> result = daoutil.queryForMapList(this, "auto_award_list");
        return result.stream().map(this::mapNegIdToNegAuthor).collect(toList());
    }

    private Entry<Long, Long> mapNegIdToNegAuthor(Map<String, Object> map) {
        Long negId = (Long) map.get("neg_id");
        Long negAuthorId = (Long) map.get("created_by");
        return new SimpleEntry<>(negId, negAuthorId);
    }

    @Override
    public List<Long> findUnlockList() {
        return daoutil.queryScalarList(this, "unlock_list");
    }

    @Override
    public void reindexBidIds(Long negId) {
        List<Map<String, Long>> bidIds = bidDAO.findNegBidIds(negId);
        Map<String, List<Map<String, Long>>> ids = Collections.singletonMap("bids", bidIds);
        searchService.updateSync("negs", negId, ids);
    }

    @Override
    public Negotiation findAndIndexSync(User user, Long negId) {
        Negotiation neg = findCustomerNeg(user, negId);
        searchService.indexSync("negs", neg.getNegId(), neg);
        return neg;
    }

    @Override
    public Negotiation findAndUpdateIndexAsync(User user, Long negId) {
        Negotiation neg = findCustomerNeg(user, negId);
        updateIndexAsync(neg);
        return neg;
    }

    @Override
    public Negotiation findAndUpdateIndexSync(User user, Long negId) {
        Negotiation neg = findCustomerNeg(user, negId);
        updateIndexSync(neg);
        return neg;
    }

    @Override
    public void updateIndexAsync(Negotiation neg) {
        searchService.updateAsync("negs", neg.getNegId(), neg);
    }

    @Override
    public void updateIndexSync(Negotiation neg) {
        searchService.updateSync("negs", neg.getNegId(), neg);
    }

    @Override
    public void deleteFromSearchIndex(Long negId) {
        searchService.deleteSync("negs", negId);
    }

    @Override
    public List<Map<String, Object>> findNotSentNegs() {
        return daoutil.queryForMapList(this, "get_for_integra");
    }

    @Override
    @SpringTransactional
    public void setSent(Long negId) {
        Object[] values = {negId};
        daoutil.dml(this, "set_sent", values);
    }

    @Override
    public void setNotificationAvaiable(Long negId) {
        Object[] values = {negId};
        daoutil.dml(this, "set_notification_available", values);
    }

    @Override
    @SpringTransactional
    public void setNotificationSent(Long negId) {
        Object[] values = {negId};
        daoutil.dml(this, "set_notification_sent", values);
    }

    @Override
    public List<Negotiation> findNotificationAvailable() {
        List<Negotiation> negotiations = daoutil.query(this, "get_for_notification");
        negotiations.forEach(this::setNegLines);
        return negotiations;
    }
}
