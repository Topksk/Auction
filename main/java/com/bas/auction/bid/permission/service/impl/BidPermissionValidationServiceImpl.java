package com.bas.auction.bid.permission.service.impl;

import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.permission.service.BidPermissionValidationService;
import com.bas.auction.bid.permission.service.CantUpdatePermissionForNegNotInVotingStatusException;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.service.NegFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidPermissionValidationServiceImpl implements BidPermissionValidationService {
    private final NegotiationDAO negDAO;
    private final BidDAO bidDAO;
    private final NegFileService negFileService;

    @Autowired
    public BidPermissionValidationServiceImpl(NegotiationDAO negDAO, BidDAO bidDAO, NegFileService negFileService) {
        this.negDAO = negDAO;
        this.bidDAO = bidDAO;
        this.negFileService = negFileService;
    }

    @Override
    public void validateUpdate(Long bidId) {
        Long negId = bidDAO.findBidNegId(bidId);
        String negStatus = negDAO.findNegStatus(negId);
        if (!"VOTING".equals(negStatus))
            throw new CantUpdatePermissionForNegNotInVotingStatusException();
        boolean isResumeReportExists = negFileService.findIsResumeReportExists(negId);
        if (isResumeReportExists)
            throw new CantUpdatePermissionForNegNotInVotingStatusException();
    }
}
