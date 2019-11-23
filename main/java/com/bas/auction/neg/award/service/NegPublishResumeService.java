package com.bas.auction.neg.award.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

import java.io.IOException;

public interface NegPublishResumeService {
    Negotiation publishResume(User user, Long negId) throws IOException;
}
