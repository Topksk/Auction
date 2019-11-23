package com.bas.auction.neg.award.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegAwardService;
import com.bas.auction.neg.award.service.NegResumeReportService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.service.NegSettingService;
import com.bas.auction.neg.unlock.service.NegUnlockService;
import com.bas.auction.plans.dao.PlanDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map.Entry;

@Service
public class NegAwardServiceImpl implements NegAwardService {
    private final Logger logger = LoggerFactory.getLogger(NegAwardServiceImpl.class);
    private final TransactionTemplate transactionTemplate;
    private NegotiationDAO negDAO;
    private final BidPermissionsService bidPermissionsService;
    private final BidLineService bidLineService;
    private final UserService userService;
    private final PlanDAO planDAO;
    private final NegUnlockService negUnlockService;
    private final NegSettingService negSettingService;
    private final NegResumeReportService negResumeReportService;
    private final RfqAwardAndRankService rfqAwardAndRankService;
    private final AuctionAwardAndRankService auctionAwardAndRankService;
    private final TenderAwardAndRankService tenderAwardAndRankService;
    private final Tender2AwardAndRankService tender2AwardAndRankService;

    @Autowired
    public NegAwardServiceImpl(TransactionTemplate transactionTemplate,
                               BidPermissionsService bidPermissionsService,
                               BidLineService bidLineService, UserService userService, PlanDAO planDAO,
                               NegUnlockService negUnlockService, NegSettingService negSettingService, NegResumeReportService negResumeReportService,
                               RfqAwardAndRankService rfqAwardAndRankService,
                               AuctionAwardAndRankService auctionAwardAndRankService,
                               TenderAwardAndRankService tenderAwardAndRankService,
                               Tender2AwardAndRankService tender2AwardAndRankService) {
        this.transactionTemplate = transactionTemplate;
        this.bidPermissionsService = bidPermissionsService;
        this.bidLineService = bidLineService;
        this.userService = userService;
        this.planDAO = planDAO;
        this.negUnlockService = negUnlockService;
        this.negSettingService = negSettingService;
        this.negResumeReportService = negResumeReportService;
        this.rfqAwardAndRankService = rfqAwardAndRankService;
        this.auctionAwardAndRankService = auctionAwardAndRankService;
        this.tenderAwardAndRankService = tenderAwardAndRankService;
        this.tender2AwardAndRankService = tender2AwardAndRankService;
    }

    @Autowired
    public void setNegotiationDAO(NegotiationDAO negDAO) {
        this.negDAO = negDAO;
    }

    @Override
    public List<Entry<Long, Long>> findAutoAwardList() {
        return negDAO.findAutoAwardList();
    }

    @Override
    @SpringTransactional
    public Negotiation manualCloseNeg(User user, Long negId) {
        logger.warn("manually closing neg: {}", negId);
        Negotiation neg = negDAO.findAdminNegHeader(negId);
        negDAO.updateActualCloseDate(user.getUserId(), negId);
        String awardMethod = negSettingService.findNegAwardMethod(negId);
        if ("AUTO".equals(awardMethod))
            neg = autoAwardImpl(negId, neg.getCreatedBy());
        else
            neg = negUnlockService.unlock(negId);
        return neg;
    }

    @Override
    public void autoAward(Long negId, Long createdBy) {
        logger.debug("Awarding neg: {}", negId);
        transactionTemplate.execute(status -> autoAward(status, negId, createdBy));
    }

    protected Void autoAward(TransactionStatus status, Long negId, Long createdBy) {
        try {
            autoAwardImpl(negId, createdBy);
        } catch (Exception e) {
            logger.error("Auto award exception: negId={}", negId, e);
            status.setRollbackOnly();
        }
        return null;
    }

    protected Negotiation autoAwardImpl(Long negId, Long createdBy) {
        Negotiation neg = negDAO.findAdminNegHeader(negId);
        // delete bid lines with 0 or empty price
        bidLineService.deleteNotParticipatedBidLines(neg);
        bidPermissionsService.createNegBidPermissions(neg, Boolean.TRUE);

        awardNeg(createdBy, neg);

        User user = userService.findById(createdBy);
        neg = negResumeReportService.generateResumeReport(user, negId, neg.getNegType());
        negDAO.updateStatus(createdBy, negId, "VOTING_FINISHED");
        neg.setNegStatus("VOTING_FINISHED");

        negDAO.updateIndexAsync(neg);
        planDAO.indexNegPlans(negId);

        return neg;
    }

    @Override
    @SpringTransactional
    public void awardNeg(User user, Long negId) {
        Negotiation neg = negDAO.findCustomerNeg(user, negId);
        awardNeg(user.getUserId(), neg);
    }

    private void awardNeg(Long userId, Negotiation neg) {
        Long negId = neg.getNegId();
        switch (neg.getNegType()) {
            case RFQ:
                rfqAwardAndRankService.finalize(userId, negId);
                break;
            case AUCTION:
                auctionAwardAndRankService.finalize(userId, negId);
                break;
            case TENDER:
                tenderAwardAndRankService.finalize(userId, negId);
                break;
            case TENDER2:
                if (neg.isTender2Stage1())
                    tender2AwardAndRankService.finalizeStage1(userId, negId);
                else
                    tender2AwardAndRankService.finalizeStage2(userId, negId);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
