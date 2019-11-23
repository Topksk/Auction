package com.bas.auction.neg.voting.service.impl;

import com.bas.auction.bid.discount.dao.BidDiscountDAO;
import com.bas.auction.bid.permission.dao.BidPermissionsDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.neg.voting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NegVotingValidationServiceImpl implements NegVotingValidationService {
    private final NegotiationDAO negDAO;
    private final BidPermissionsDAO bidPermissionsDAO;
    private final BidDiscountDAO bidDiscountDAO;
    private final NegFileService negFileService;

    @Autowired
    public NegVotingValidationServiceImpl(NegotiationDAO negDAO, BidPermissionsDAO bidPermissionsDAO, BidDiscountDAO bidDiscountDAO, NegFileService negFileService) {
        this.negDAO = negDAO;
        this.bidPermissionsDAO = bidPermissionsDAO;
        this.bidDiscountDAO = bidDiscountDAO;
        this.negFileService = negFileService;
    }

    @Override
    public void validateFinishVoting(Long negId) {
        validateFinishingNegStatus(negId);
        boolean isResumeReportExists = negFileService.findIsResumeReportExists(negId);
        if (isResumeReportExists)
            throw new CantFinishVotingForNegNotInVotingStatusException();
        validatePermissions(negId);
        validateDiscounts(negId);
    }

    @Override
    public void validateResumeVoting(Long negId) {
        validateResumingNegStatus(negId);
        boolean isResumeReportExists = negFileService.findIsResumeReportExists(negId);
        if (isResumeReportExists)
            throw new CantResumeVotingForNegNotInVotingFinishedStatusException();
    }

    private void validateFinishingNegStatus(Long negId) {
        String negStatus = negDAO.findNegStatus(negId);
        if (!"VOTING".equals(negStatus))
            throw new CantFinishVotingForNegNotInVotingStatusException();
    }

    private void validateResumingNegStatus(Long negId) {
        String negStatus = negDAO.findNegStatus(negId);
        if (!"VOTING_FINISHED".equals(negStatus))
            throw new CantResumeVotingForNegNotInVotingFinishedStatusException();
    }

    private void validatePermissions(Long negId) {
        List<Integer> negInvalidPermissionBidLines = bidPermissionsDAO.findNegInvalidPermissionBidLines(negId);
        if (!negInvalidPermissionBidLines.isEmpty())
            throw new EitherPermitOrRejectLines(negInvalidPermissionBidLines);
    }

    private void validateDiscounts(Long negId) {
        NegType negType = negDAO.findNegType(negId);
        if (negType != NegType.TENDER && negType != NegType.TENDER2)
            return;
        List<Integer> discountNotConfirmedLineNums = bidDiscountDAO.findDiscountNotConfirmedLineNums(negId);
        if (!discountNotConfirmedLineNums.isEmpty())
            throw new ConfirmAllDiscountsException(discountNotConfirmedLineNums);
    }
}