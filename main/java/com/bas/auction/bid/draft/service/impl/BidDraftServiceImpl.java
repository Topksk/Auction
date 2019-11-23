package com.bas.auction.bid.draft.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.draft.service.*;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.core.Conf;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation.NegType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BidDraftServiceImpl implements BidDraftService {
    private final Logger logger = LoggerFactory.getLogger(BidDraftServiceImpl.class);
    private final BidDAO bidDAO;
    private final NegotiationDAO negDAO;
    private final BidLineService bidLineService;
    private final BidDiscountService bidDiscountService;
    private final BidFileService bidFileService;

    @Autowired
    public BidDraftServiceImpl(BidDAO bidDAO, Conf conf, NegotiationDAO negDAO, BidLineService bidLineService,
                               BidDiscountService bidDiscountService, BidFileService bidFileService) {
        this.bidDAO = bidDAO;
        this.negDAO = negDAO;
        this.bidLineService = bidLineService;
        this.bidDiscountService = bidDiscountService;
        this.bidFileService = bidFileService;
    }

    @Override
    @SpringTransactional
    public Bid create(User user, Long negId, String currencyCode) {
        logger.debug("create bid for neg: {}", negId);
        if (bidDAO.activeBidExists(user.getSupplierId(), negId)) {
            throw new ActiveBidAlreadyExistsException();
        }
        boolean isTender2Stage2 = negDAO.findIsTender2Stage2(negId);
        Long stage1PermittedBidId = null;
        if (isTender2Stage2) {
            Long parentNegId = negDAO.findParentNegId(negId);
            stage1PermittedBidId = bidDAO.findSupplierTender2Stage1PermittedBidId(user.getSupplierId(), parentNegId);
            if (stage1PermittedBidId == null)
                throw new NoPermittedTender2Stage1BidException();
        }
        Bid bid = createBidDto(negId, user.getSupplierId(), currencyCode);
        bid = bidDAO.insert(user, bid);
        if (isTender2Stage2) {
            bidLineService.copyBidLinesForTender2Stage2(user, stage1PermittedBidId, bid.getBidId());
            bidDiscountService.copyBidActiveDiscounts(user, stage1PermittedBidId, bid.getBidId());
        } else {
            bidLineService.createBidLinesFromNeg(user, bid.getNegId(), bid.getBidId());
            createBidDiscounts(user, bid);
        }

        bid = bidDAO.findAndIndexSync(user, bid.getBidId());
        negDAO.reindexBidIds(negId);
        return bid;
    }

    protected Bid createBidDto(Long negId, Long supplierId, String currencyCode) {
        Bid bid = new Bid();
        bid.setNegId(negId);
        bid.setSupplierId(supplierId);
        bid.setBidStatus("DRAFT");
        bid.setCurrencyCode(currencyCode);
        return bid;
    }

    private void createBidDiscounts(User user, Bid bid) {
        NegType negType = negDAO.findNegType(bid.getNegId());
        if (negType == NegType.TENDER || negType == NegType.TENDER2) {
            bidDiscountService.createBidDiscounts(user, bid.getNegId(), bid.getBidId());
        }
    }

    @Override
    @SpringTransactional
    public void updateWithoutIndexing(User user, Bid bid) {
        logger.debug("update bid: bidId={}", bid.getBidId());
        validateBidStatus(bid.getBidId());
        bidDAO.update(user, bid);
        bidLineService.update(user, bid.getBidId(), bid.getBidLines());
    }

    @Override
    @SpringTransactional
    public Bid update(User user, Bid bid) {
        logger.debug("update bid: bidId={}", bid.getBidId());
        validateBidStatus(bid.getBidId());
        bidDAO.update(user, bid);
        bidLineService.update(user, bid.getBidId(), bid.getBidLines());
        return bidDAO.findAndIndexSync(user, bid.getBidId());
    }

    @Override
    @SpringTransactional
    public Bid updateCurrency(User user, Long bidId, String currency) {
        logger.debug("update bid currency: bidId={}, currency={}", bidId, currency);
        validateBidStatus(bidId);
        bidDAO.updateCurrency(user, bidId, currency);
        return bidDAO.findAndIndexSync(user, bidId);
    }

    @Override
    @SpringTransactional
    public void delete(User user, Long bidId) throws IOException {
        logger.info("delete bid: {}", bidId);
        if (!bidDAO.findIsDraft(bidId))
            throw new OnlyDraftBidCanBeDeleted();
        Long negId = bidDAO.findBidNegId(bidId);
        bidDiscountService.deleteBidDiscounts(bidId);
        bidLineService.deleteBidLines(bidId);
        bidDAO.delete(user, bidId);
        bidFileService.deleteBidAllFiles(user, bidId);
        bidDAO.deleteFromSearchIndexSync(bidId);
        negDAO.reindexBidIds(negId);
    }

    @Override
    public Bid deleteFiles(User user, Long bidId, List<Long> ids) throws IOException {
        if (!bidDAO.findIsDraft(bidId))
            throw new CantUpdateNotDraftBidException();
        boolean isBidReportExists = bidFileService.findIsBidReportExists(bidId);
        boolean isBidParticipationReportExists = bidFileService.findIsBidParticipationReportExists(bidId);
        if(isBidReportExists || isBidParticipationReportExists) {
            boolean eitherBidOrParticipationReport = ids.stream()
                    .allMatch(id -> bidFileService.isBidReportFileId(id) || bidFileService.isBidParticipationFileId(id));
            if(!eitherBidOrParticipationReport)
                throw new CantUpdateNotDraftBidException();
        }
        bidFileService.deleteBidFiles(user, bidId, ids);
        return bidDAO.findAndIndexSync(user, bidId);
    }

    private void validateBidStatus(Long bidId) {
        boolean isBidReportExists = bidFileService.findIsBidReportExists(bidId);
        boolean isBidParticipationReportExists = bidFileService.findIsBidParticipationReportExists(bidId);
        if (isBidReportExists || isBidParticipationReportExists)
            throw new CantUpdateNotDraftBidException();
        boolean isDraft = bidDAO.findIsDraft(bidId);
        if (!isDraft)
            throw new CantUpdateNotDraftBidException();
    }
}
