package com.bas.auction.bid.service;

import com.bas.auction.auth.dto.User;

public interface BidReportService {
	void generateBidReport(User user, Long bidId) throws Exception;

	void generateBidParticipationAppl(User user, Long bidId) throws Exception;
}
