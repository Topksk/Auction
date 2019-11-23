package com.bas.auction.neg.service;

import com.bas.auction.auth.dto.User;

public interface NegReportsService {
	void generatePublishReport(User user, Long negId);

	void generateUnlockReport(User user, Long negId);

	void generateRfqResumeReport(User user, Long negId);

	void generateAuctionResumeReport(User user, Long negId);

	void generateTenderResumeReport(User user, Long negId);

	void generateTender2Stage1ResumeReport(User user, Long negId);

	void generateTender2Stage2ResumeReport(User user, Long negId);

	void generateFailedTender2Stage2ResumeReport(User user, Long negId);
}
