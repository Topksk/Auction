package com.bas.auction.neg.award.service.tiebreaker;

import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.dto.BidLine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.partitioningBy;

public class DomesticWorkServiceCriteriaBasedTiebreaker implements CriteriaBasedTiebreaker {
    private final BidDiscountService bidDiscountService;
    private final Long negId;

    public DomesticWorkServiceCriteriaBasedTiebreaker(BidDiscountService bidDiscountService, Long negId) {
        this.bidDiscountService = bidDiscountService;
        this.negId = negId;
    }

    @Override
    public void breakTies(List<BidLine> rankTiedBidLines) {
        Map<Boolean, List<BidLine>> domesticProducerPartition = partitionByDomesticWorkServiceCriteria(
                rankTiedBidLines);
        List<BidLine> domesticWorkServiceBidLines = domesticProducerPartition.get(Boolean.TRUE);
        List<BidLine> notDomesticWorkServiceBidLines = domesticProducerPartition.get(Boolean.FALSE);
        int incrementValue = Optional.ofNullable(domesticWorkServiceBidLines).map(List::size).orElse(0);
        Optional.ofNullable(notDomesticWorkServiceBidLines).ifPresent(bls -> incrementBidRanks(bls, incrementValue));
    }

    private Map<Boolean, List<BidLine>> partitionByDomesticWorkServiceCriteria(List<BidLine> rankTiedBidLines) {
        Map<Long, Boolean> isDomesticProducers = bidDiscountService.findIsDomesticWorkService(negId,
                rankTiedBidLines);
        Predicate<BidLine> predicate = bl -> isDomesticProducers.get(bl.getBidId());
        return rankTiedBidLines.stream().collect(partitioningBy(predicate));
    }

    private void incrementBidRanks(List<BidLine> bidLines, int incrementValue) {
        bidLines.forEach(bl -> bl.setRank(bl.getRank() + incrementValue));
    }

}
