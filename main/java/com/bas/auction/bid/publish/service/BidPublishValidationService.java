package com.bas.auction.bid.publish.service;


import com.bas.auction.neg.dto.Negotiation;

public interface BidPublishValidationService {
    void validateBidSend(Long bidId, Long negId, Negotiation.NegType negType);
}
