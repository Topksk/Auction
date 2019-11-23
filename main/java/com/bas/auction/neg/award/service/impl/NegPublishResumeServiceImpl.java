package com.bas.auction.neg.award.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegPublishResumeService;
import com.bas.auction.neg.award.service.NegPublishResumeValidationService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.draft.service.NegDraftService;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.neg.service.NegNotificationService;
import com.bas.auction.plans.dao.PlanDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NegPublishResumeServiceImpl implements NegPublishResumeService {
    private final Logger logger = LoggerFactory.getLogger(NegPublishResumeServiceImpl.class);
    private final NegotiationDAO negDAO;
    private final PlanDAO planDAO;
    private final BidDAO bidDAO;
    private final NegPublishResumeValidationService negPublishResumeValidationService;
    private final NegNotificationService negNotifService;
    private final NegFileService negFileService;
    private final NegDraftService negDraftService;

    @Autowired
    public NegPublishResumeServiceImpl(NegotiationDAO negDAO, PlanDAO planDAO, BidDAO bidDAO, NegPublishResumeValidationService negPublishResumeValidationService, NegNotificationService negNotifService,
                                       NegFileService negFileService, NegDraftService negDraftService) {
        this.negDAO = negDAO;
        this.planDAO = planDAO;
        this.bidDAO = bidDAO;
        this.negPublishResumeValidationService = negPublishResumeValidationService;
        this.negNotifService = negNotifService;
        this.negFileService = negFileService;
        this.negDraftService = negDraftService;
    }

    @Override
    @SpringTransactional
    public Negotiation publishResume(User user, Long negId) throws IOException {
        logger.debug("publish resume: {}", negId);
        negPublishResumeValidationService.validatePublishResume(negId);

        boolean isTender2Stage1 = negDAO.findIsTender2Stage1(negId);

        negDAO.updateAwardDate(user.getUserId(), negId);
        updateResumePublishedNegFiles(user.getUserId(), negId, isTender2Stage1);
        updateResumePublishedNegStatus(user.getUserId(), negId, isTender2Stage1);
        updateResumePublishedNegPlan(user.getUserId(), negId, isTender2Stage1);

        Negotiation neg = negDAO.findAndUpdateIndexAsync(user, negId);
        negNotifService.sendResumeRepPublishNotif(neg);
        if (neg.isTender2Stage1() && !neg.isFailed())
            neg = createTender2Stage2(user, neg);

        bidDAO.reindexNegBidHeaders(negId);
        negFileService.makeNegResumeReportPublicAccessible(negId);
        return neg;
    }

    private void updateResumePublishedNegFiles(Long userId, Long negId, boolean isTender2Stage1) {
        negFileService.deleteNegFilesCustomerOnlyAttr(negId);
        if (isTender2Stage1)
            negFileService.makeNegFilesCustomerSignOnly(userId, negId);
        else
            negFileService.makeNegFilesReadOnly(userId, negId);
    }

    private void updateResumePublishedNegStatus(Long userId, Long negId, boolean isTender2Stage1) {
        String negStatus;
        if (isTender2Stage1) {
            boolean permittedNegLineExists = negDAO.permittedTender2Stage1LineExists(negId);
            negStatus = permittedNegLineExists ? "FIRST_STAGE_FINISHED" : "NEG_FAILED";
        } else {
            boolean awardedNegLineExists = negDAO.awardedNegLineExists(negId);
            negStatus = awardedNegLineExists ? "AWARDED" : "NEG_FAILED";
        }
        negDAO.updateStatus(userId, negId, negStatus);
    }

    private void updateResumePublishedNegPlan(Long userId, Long negId, boolean isTender2Stage1) {
        if (isTender2Stage1)
            planDAO.updateFailedNegPlansStatus(userId, negId);
        else
            planDAO.updateResumePublishedNegPlansStatus(userId, negId);
        planDAO.indexNegPlans(negId);
    }

    private Negotiation createTender2Stage2(User user, Negotiation neg) throws IOException {
        String docNumber = neg.getDocNumber() + "-2";
        return negDraftService.copyNeg(user, neg.getNegId(), docNumber, 2, true);
    }
}
