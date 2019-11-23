package com.bas.auction.bid.discount.service.impl;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.service.BidDiscountValidationService;
import com.bas.auction.bid.discount.service.CantUpdateDiscountForNegNotInVotingStatusException;
import com.bas.auction.bid.draft.service.CantUpdateNotDraftBidException;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.service.NegFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidDiscountValidationServiceImpl implements BidDiscountValidationService {
    private final NegotiationDAO negDAO;
    private final BidDAO bidDAO;
    private final BidFileService bidFileService;
    private final NegFileService negFileService;

    @Autowired
    public BidDiscountValidationServiceImpl(NegotiationDAO negDAO, BidDAO bidDAO, BidFileService bidFileService, NegFileService negFileService) {
        this.negDAO = negDAO;
        this.bidDAO = bidDAO;
        this.bidFileService = bidFileService;
        this.negFileService = negFileService;
    }

    @Override
    public void validateUpdate(Long bidId) {
        boolean isBidReportExists = bidFileService.findIsBidReportExists(bidId);
        boolean isBidParticipationReportExists = bidFileService.findIsBidParticipationReportExists(bidId);
        if (isBidReportExists || isBidParticipationReportExists)
            throw new CantUpdateNotDraftBidException();
        boolean isDraft = bidDAO.findIsDraft(bidId);
        if (!isDraft)
            throw new CantUpdateNotDraftBidException();
    }

    @Override
    public void validateCorrection(Long negId) {
        validateNegStatus(negId);
    }

    private void validateNegStatus(Long negId) {
        String negStatus = negDAO.findNegStatus(negId);
        if (!"VOTING".equals(negStatus))
            throw new CantUpdateDiscountForNegNotInVotingStatusException();
        boolean isResumeReportExists = negFileService.findIsResumeReportExists(negId);
        if (isResumeReportExists)
            throw new CantUpdateDiscountForNegNotInVotingStatusException();
    }
}
