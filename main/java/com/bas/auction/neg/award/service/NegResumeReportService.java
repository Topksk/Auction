package com.bas.auction.neg.award.service;


import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

public interface NegResumeReportService {
    Negotiation generateResumeReport(User user, Long negId);

    Negotiation generateResumeReport(User user, Long negId, Negotiation.NegType negType);
}
