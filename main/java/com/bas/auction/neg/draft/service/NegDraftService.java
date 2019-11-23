package com.bas.auction.neg.draft.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;

import java.io.IOException;
import java.util.List;

public interface NegDraftService {
    Negotiation create(User user, String title, String negType);

    Negotiation copyNeg(User user, Long negId, String docNumber, Integer stage,
                        boolean copyProtocols) throws IOException;

    Negotiation update(User user, Negotiation neg);

    void delete(User user, Long negId) throws IOException;

    Negotiation deleteFiles(User user, Long negId, List<Long> fileIds) throws IOException;
}
