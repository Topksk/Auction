package com.bas.auction.neg.publish.service;


public interface NegPublishValidationService {
    void validatePublish(Long negId);

    void validatePublishReport(Long negId);
}
