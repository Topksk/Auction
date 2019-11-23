package com.bas.auction.bid.publish.service;

import com.bas.auction.auth.dto.User;

public interface BidReportValidationService {
    void validateBidReport(User user, Long bidId);

    void validateTender2Stage1BidParticipationAppl(User user, Long bidId);
}
