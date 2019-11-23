package com.bas.auction.neg.publish.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

import java.io.IOException;

public interface NegPublishService {

    Negotiation publish(User user, Long negId) throws IOException;

    void generatePublishReport(User user, Long negId);
}
