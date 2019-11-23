package com.bas.auction.bid.publish.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;

import javax.mail.MessagingException;

public interface BidPublishService {
	Bid send(User user, Long bidId) throws MessagingException;

	Bid generateBidReport(User user, Long bidId) throws Exception;

	Bid generateBidParticipationAppl(User user, Long bidId) throws Exception;

	Bid generateBidReportAndParticipationAppl(User user, Long bidId) throws Exception;
}
