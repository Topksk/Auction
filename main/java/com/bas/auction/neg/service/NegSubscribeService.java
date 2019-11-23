package com.bas.auction.neg.service;

import com.bas.auction.neg.dto.Negotiation;

import java.util.List;

/**
 * Created by bayangali.nauryz on 07.01.2016.
 */
public interface NegSubscribeService {
    void subscriberNotification(Negotiation neg);
    void subscriberNotification(List<Negotiation> negs);
    void notSubscriberNotification(List<Negotiation> negs);
}
