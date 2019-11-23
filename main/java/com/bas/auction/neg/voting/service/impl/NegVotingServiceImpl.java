package com.bas.auction.neg.voting.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegAwardService;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.voting.service.NegVotingService;
import com.bas.auction.neg.voting.service.NegVotingValidationService;
import com.bas.auction.plans.dao.PlanDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegVotingServiceImpl implements NegVotingService {
    private final Logger logger = LoggerFactory.getLogger(NegVotingServiceImpl.class);
    private final NegotiationDAO negDAO;
    private final NegLineDAO negLineDAO;
    private final NegAwardService negAwardService;
    private final BidLineService bidLineService;
    private final BidDAO bidDAO;
    private final PlanDAO planDAO;
    private final NegVotingValidationService negVotingValidationService;

    @Autowired
    public NegVotingServiceImpl(NegotiationDAO negDAO, NegLineDAO negLineDAO, NegAwardService negAwardService,
                                BidDAO bidDAO, PlanDAO planDAO, BidLineService bidLineService, NegVotingValidationService negVotingValidationService) {
        this.negDAO = negDAO;
        this.negLineDAO = negLineDAO;
        this.negAwardService = negAwardService;
        this.bidDAO = bidDAO;
        this.planDAO = planDAO;
        this.bidLineService = bidLineService;
        this.negVotingValidationService = negVotingValidationService;
    }

    @Override
    @SpringTransactional
    public Negotiation finishVoting(User user, Long negId) {
        logger.debug("finish voting: {}", negId);
        negVotingValidationService.validateFinishVoting(negId);
        negAwardService.awardNeg(user, negId);
        updateStatusToVotingFinished(user.getUserId(), negId);
        Negotiation neg = negDAO.findCustomerNeg(user, negId);
        negDAO.updateIndexAsync(neg);
        return neg;
    }

    @Override
    @SpringTransactional
    public Negotiation resumeVoting(User user, Long negId) {
        logger.debug("resume voting: {}", negId);
        negVotingValidationService.validateResumeVoting(negId);
        Long userId = user.getUserId();
        bidLineService.resetAwardStatusesAndRanks(userId, negId);
        bidDAO.resetAwardStatuses(userId, negId);
        planDAO.resetNegPlanAwardStatuses(userId, negId);
        negLineDAO.resetAwards(userId, negId);
        updateStatusToVoting(userId, negId);
        Negotiation neg = negDAO.findCustomerNeg(user, negId);
        negDAO.updateIndexAsync(neg);
        return neg;
    }

    private void updateStatusToVotingFinished(Long userId, Long negId) {
        negDAO.updateStatus(userId, negId, "VOTING_FINISHED");
    }

    private void updateStatusToVoting(Long userId, Long negId) {
        negDAO.updateStatus(userId, negId, "VOTING");
    }
}
