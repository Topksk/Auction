package com.bas.auction.neg.award.service.tiebreaker;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.neg.dao.NegLineDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

public class BidRanksTiebreaker {
    private final Logger logger = LoggerFactory.getLogger(BidRanksTiebreaker.class);
    private final NegLineDAO negLineDAO;
    private final BidDiscountService bidDiscountService;
    private final BidDAO bidDAO;
    private final Long negId;
    private final Integer lineNum;
    private List<BidLine> rankedBidLines;
    private Map<Integer, List<BidLine>> tiedBidRankGroups;

    public BidRanksTiebreaker(NegLineDAO negLineDAO, BidDiscountService bidDiscountService, BidDAO bidDAO, Long negId, Integer lineNum) {
        this.negLineDAO = negLineDAO;
        this.bidDiscountService = bidDiscountService;
        this.bidDAO = bidDAO;
        this.negId = negId;
        this.lineNum = lineNum;
    }

    public void breakTies(List<BidLine> rankedBidLines) {
        this.rankedBidLines = rankedBidLines;
        if (!hasTiedRanks())
            return;
        logger.debug("start breaking ties");
        breakTiesUsingDomesticCriteria();
        if (!hasTiedRanks())
            return;
        // we still have tie
        breakTiesUsingExperienceCriteria();
        if (!hasTiedRanks())
            return;
        // we still have tie
        breakTiesUsingBidPublishDateCriteria();
        // ensure tie break by assigning bid list indexes to bid ranks
        // list indexes should be ultimate bid ranks
        breakTiesUsingBidListIndex();
    }

    protected boolean hasTiedRanks() {
        Stream<Entry<Integer, List<BidLine>>> rankGroups = groupBidLinesByRanks().entrySet().stream();
        tiedBidRankGroups = rankGroups.filter(this::hasTie).collect(toMap(Entry::getKey, Entry::getValue));
        return !tiedBidRankGroups.isEmpty();
    }

    protected Map<Integer, List<BidLine>> groupBidLinesByRanks() {
        return rankedBidLines.stream().collect(groupingBy(BidLine::getRank));
    }

    protected boolean hasTie(Entry<Integer, List<BidLine>> entry) {
        return entry.getValue().size() > 1;
    }

    protected void breakTiesUsingDomesticCriteria() {
        String purchaseType = negLineDAO.findNegLinePurchaseType(negId, lineNum);
        logger.debug("breaking ties using domestic criteria for {}", purchaseType);
        CriteriaBasedTiebreaker tiebreaker = buildDomesticCriteriaBasedTiebreaker(purchaseType);
        tiedBidRankGroups.values().forEach(tiebreaker::breakTies);
    }

    protected CriteriaBasedTiebreaker buildDomesticCriteriaBasedTiebreaker(String purchaseType) {
        if (isGood(purchaseType))
            return new DomesticGoodsProducerCriteriaBasedTiebreaker(bidDiscountService, negId);
        else if (isWorkService(purchaseType))
            return new DomesticWorkServiceCriteriaBasedTiebreaker(bidDiscountService, negId);
        throw new IllegalArgumentException();
    }

    protected void breakTiesUsingExperienceCriteria() {
        logger.debug("breaking ties using experience criteria");
        CriteriaBasedTiebreaker tiebreaker = buildExperienceCriteriaBasedTiebreaker();
        tiedBidRankGroups.values().forEach(tiebreaker::breakTies);
    }

    protected CriteriaBasedTiebreaker buildExperienceCriteriaBasedTiebreaker() {
        return new ExperienceCriteriaBasedTiebreaker(bidDiscountService, negId);
    }

    protected void breakTiesUsingBidPublishDateCriteria() {
        CriteriaBasedTiebreaker tiebreaker = buildBidPublishDateCriteriaBasedTiebreaker();
        tiedBidRankGroups.values().forEach(tiebreaker::breakTies);
    }

    protected CriteriaBasedTiebreaker buildBidPublishDateCriteriaBasedTiebreaker() {
        return new BidPublishDateCriteriaBasedTiebreaker(bidDAO);
    }

    protected void breakTiesUsingBidListIndex() {
        Comparator<BidLine> c = (bl1, bl2) -> bl1.getRank().compareTo(bl2.getRank());
        rankedBidLines.sort(c);
        // list indexes should be ultimate bid ranks
        for (int i = 0; i < rankedBidLines.size(); i++)
            rankedBidLines.get(i).setRank(i + 1);
    }

    private boolean isGood(String purchaseType) {
        return "GOOD".equals(purchaseType);
    }

    private boolean isWorkService(String purchaseType) {
        return "WORK".equals(purchaseType) || "SERVICE".equals(purchaseType);
    }

}
