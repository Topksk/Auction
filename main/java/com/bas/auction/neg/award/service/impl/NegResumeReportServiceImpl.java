package com.bas.auction.neg.award.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.award.service.NegResumeReportService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.service.NegNotificationService;
import com.bas.auction.neg.service.NegReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NegResumeReportServiceImpl implements NegResumeReportService {
    private final NegotiationDAO negDAO;
    private final NegReportsService negReports;
    private final NegNotificationService negNotifService;

    @Autowired
    public NegResumeReportServiceImpl(NegotiationDAO negDAO, NegReportsService negReports, NegNotificationService negNotifService) {
        this.negDAO = negDAO;
        this.negReports = negReports;
        this.negNotifService = negNotifService;
    }

    @Override
    @SpringTransactional
    public Negotiation generateResumeReport(User user, Long negId) {
        NegType negType = negDAO.findNegType(negId);
        return generateResumeReport(user, negId, negType);
    }

    @Override
    public Negotiation generateResumeReport(User user, Long negId, NegType negType) {
        switch (negType) {
            case RFQ:
                negReports.generateRfqResumeReport(user, negId);
                break;
            case AUCTION:
                negReports.generateAuctionResumeReport(user, negId);
                break;
            case TENDER:
                negReports.generateTenderResumeReport(user, negId);
                break;
            case TENDER2:
                generateTender2ResumeReport(user, negId);
                break;
        }
        Negotiation neg = negDAO.findCustomerNeg(user, negId);
        negNotifService.sendResumeRepNotif(neg);
        return neg;
    }

    private void generateTender2ResumeReport(User user, Long negId) {
        Integer stage = negDAO.findNegStage(negId);
        if (stage == 1) {
            negReports.generateTender2Stage1ResumeReport(user, negId);
            boolean notFailedNegLineExists = negDAO.notFailedNegLineExists(negId);
            if (!notFailedNegLineExists)
                negReports.generateFailedTender2Stage2ResumeReport(user, negId);
        } else
            negReports.generateTender2Stage2ResumeReport(user, negId);
    }

}
