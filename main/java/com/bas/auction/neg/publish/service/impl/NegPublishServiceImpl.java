package com.bas.auction.neg.publish.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.publish.service.NegPublishService;
import com.bas.auction.neg.publish.service.NegPublishValidationService;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.neg.service.NegNotificationService;
import com.bas.auction.neg.service.NegReportsService;
import com.bas.auction.neg.service.NegSubscribeService;
import com.bas.auction.plans.dao.PlanDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NegPublishServiceImpl implements NegPublishService {
    private final Logger logger = LoggerFactory.getLogger(NegPublishServiceImpl.class);
    private final NegotiationDAO negDAO;
    private final NegFileService negFileService;
    private final NegReportsService negReports;
    private final PlanDAO planDAO;
    private final NegNotificationService negNotifService;
    private final NegPublishValidationService publishValidationService;
    private final NegSubscribeService negSubscribeService;


    @Autowired
    public NegPublishServiceImpl(NegotiationDAO negDAO, NegFileService negFileService,
                                 NegReportsService negReports, PlanDAO planDAO, NegNotificationService negNotifService,
                                 NegPublishValidationService publishValidationService, NegSubscribeService negSubscribeService) {
        this.negDAO = negDAO;
        this.negFileService = negFileService;
        this.negReports = negReports;
        this.planDAO = planDAO;
        this.negNotifService = negNotifService;
        this.publishValidationService = publishValidationService;
        this.negSubscribeService = negSubscribeService;
    }

    @Override
    @SpringTransactional
    public Negotiation publish(User user, Long negId) throws IOException {
        logger.debug("publishing neg: {}", negId);
        publishValidationService.validatePublish(negId);
        negFileService.makeNegFilesReadOnly(user.getUserId(), negId);
        negDAO.updateStatus(user.getUserId(), negId, "PUBLISHED");
        planDAO.updatePublishedNegPlansStatus(user.getUserId(), negId);
        Negotiation neg = negDAO.findAndUpdateIndexSync(user, negId);
        planDAO.indexNegPlans(negId);
        if (neg.isTender2Stage2()) {
            negNotifService.sendTender2Stage2PublishNotif(neg);
        } else {
            negSubscribeService.subscriberNotification(neg);
        }

        negFileService.makeNegPublishReportPublicAccessible(negId);
        return neg;
    }

    @Override
    @SpringTransactional
    public void generatePublishReport(User user, Long negId) {
        publishValidationService.validatePublishReport(negId);
        negReports.generatePublishReport(user, negId);
    }
}
