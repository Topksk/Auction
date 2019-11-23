package com.bas.auction.neg.service;

import com.bas.auction.neg.dto.Negotiation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public interface NegNotificationService {
	List<Optional<Future<Void>>> sendTender2Stage2PublishNotif(Negotiation neg);

	List<Optional<Future<Void>>> sendResumeRepPublishNotif(Negotiation neg);

	List<Optional<Future<Void>>> sendUnlockRepPublishNotif(Negotiation neg);

	List<Optional<Future<Void>>> sendResumeRepNotif(Negotiation neg);
}
