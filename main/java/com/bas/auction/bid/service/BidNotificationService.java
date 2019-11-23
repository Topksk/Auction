package com.bas.auction.bid.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;

import javax.mail.MessagingException;

public interface BidNotificationService {
	void sendAuctionPriceChangeNotif(User user, Bid bid) throws MessagingException;

	void sendBidSentNotification(User user, Bid bid) throws MessagingException;
}
