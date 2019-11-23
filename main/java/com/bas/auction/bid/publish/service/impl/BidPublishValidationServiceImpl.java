package com.bas.auction.bid.publish.service.impl;


import com.bas.auction.bid.auction.service.AuctionBidValidationService;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.publish.service.BidAlreadySentException;
import com.bas.auction.bid.publish.service.BidParticipationReportRequiredException;
import com.bas.auction.bid.publish.service.BidPublishValidationService;
import com.bas.auction.bid.publish.service.BidReportRequiredException;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.bid.service.BidSendFinishedException;
import com.bas.auction.docfiles.dao.AllDocFilesShouldBeSignedException;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BidPublishValidationServiceImpl implements BidPublishValidationService {
    private final NegotiationDAO negDAO;
    private final BidDAO bidDAO;
    private final BidFileService bidFileService;
    private final AuctionBidValidationService auctionBidValidationService;

    @Autowired
    public BidPublishValidationServiceImpl(NegotiationDAO negDAO, BidDAO bidDAO, BidFileService bidFileService,
                                           AuctionBidValidationService auctionBidValidationService) {
        this.negDAO = negDAO;
        this.bidDAO = bidDAO;
        this.bidFileService = bidFileService;
        this.auctionBidValidationService = auctionBidValidationService;
    }

    @Override
    public void validateBidSend(Long bidId, Long negId, NegType negType) {
        boolean isPublishedNeg = negDAO.findIsPublishedNeg(negId);
        if (!isPublishedNeg)
            throw new BidSendFinishedException();
        validateBidStatus(bidId);
        validateBidFiles(bidId, negId, negType);
        if (negType == NegType.AUCTION)
            auctionBidValidationService.validateBidPrice(bidId);
    }

    protected void validateBidStatus(Long bidId) {
        boolean isDraft = bidDAO.findIsDraft(bidId);
        if (!isDraft)
            throw new BidAlreadySentException();
    }

    protected void validateBidFiles(Long bidId, Long negId, NegType negType) {
        boolean unsignedFileExists = bidFileService.findIsUnsignedFilesExists(bidId);
        if (unsignedFileExists)
            throw new AllDocFilesShouldBeSignedException();
        if (negType == NegType.TENDER2) {
            validateTender2BidFiles(bidId, negId);
        } else {
            validateBidReport(bidId);
            if (negType == NegType.TENDER)
                validateBidParticipationReport(bidId);
        }
    }

    protected void validateBidReport(Long bidId) {
        boolean isBidReportExists = bidFileService.findIsBidReportExists(bidId);
        if (!isBidReportExists)
            throw new BidReportRequiredException();
    }

    protected void validateBidParticipationReport(Long bidId) {
        boolean isBidParticipationReportExists = bidFileService.findIsBidParticipationReportExists(bidId);
        if (!isBidParticipationReportExists)
            throw new BidParticipationReportRequiredException();
    }

    protected void validateTender2BidFiles(Long bidId, Long negId) {
        boolean isTender2Stage1 = negDAO.findIsTender2Stage1(negId);
        if (isTender2Stage1) {
            validateBidParticipationReport(bidId);
        } else {
            validateBidReport(bidId);
        }
    }
}