package com.bas.auction.bid.discount.service;

public interface BidDiscountValidationService {
    void validateUpdate(Long bidId);

    void validateCorrection(Long bidId);
}
