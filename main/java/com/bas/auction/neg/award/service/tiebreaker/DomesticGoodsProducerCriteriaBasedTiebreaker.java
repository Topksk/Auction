package com.bas.auction.neg.award.service.tiebreaker;

import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.partitioningBy;

public class DomesticGoodsProducerCriteriaBasedTiebreaker implements CriteriaBasedTiebreaker {
    private final Logger logger = LoggerFactory.getLogger(DomesticGoodsProducerCriteriaBasedTiebreaker.class);
    private final BidDiscountService bidDiscountService;
    private final Long negId;

    public DomesticGoodsProducerCriteriaBasedTiebreaker(BidDiscountService bidDiscountService, Long negId) {
        this.bidDiscountService = bidDiscountService;
        this.negId = negId;
    }

    @Override
    public void breakTies(List<BidLine> rankTiedBidLines) {
        logger.debug("break ties using domestic good producer criteria");
        Map<Boolean, List<BidLine>> domesticProducerPartition = partitionByDomesticGoodsProducerCriteria(
                rankTiedBidLines);
        List<BidLine> domesticProducersBidLines = domesticProducerPartition.get(Boolean.TRUE);
        List<BidLine> notDomesticProducersBidLines = domesticProducerPartition.get(Boolean.FALSE);
        int incrementValue = Optional.ofNullable(domesticProducersBidLines).map(List::size).orElse(0);
        logger.debug("increment not domestic producers rank to {}", incrementValue);
        Optional.ofNullable(notDomesticProducersBidLines).ifPresent(bls -> incrementBidRanks(bls, incrementValue));
    }

    private Map<Boolean, List<BidLine>> partitionByDomesticGoodsProducerCriteria(List<BidLine> rankTiedBidLines) {
        Map<Long, Boolean> isDomesticProducers = bidDiscountService.findIsDomesticProducerOfGoods(negId, rankTiedBidLines);
        Predicate<BidLine> predicate = bl -> isDomesticProducers.get(bl.getBidId());
        return rankTiedBidLines.stream()
                .collect(partitioningBy(predicate));
    }

    private void incrementBidRanks(List<BidLine> bidLines, int incrementValue) {
        bidLines.forEach(bl -> bl.setRank(bl.getRank() + incrementValue));
    }

}
