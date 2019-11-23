package com.bas.auction.bid.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.docfiles.dto.DocFile;

import java.io.IOException;
import java.util.List;

public interface BidFileService {
	List<DocFile> findBidFiles(User user, Long bidId);

	void makeBidFilesReadOnly(Long userId, Long bidId);

	void deleteBidFilesReadOnlyAttr(Long bidId);

	boolean findIsUnsignedFilesExists(Long bidId);

	boolean findIsBidReportExists(Long bidId);

	boolean findIsBidParticipationReportExists(Long bidId);

	void copyBidFiles(User user, Long sourceBidId, Long destinationBidId) throws IOException;

	void deleteBidReport(User user, Long bidId) throws IOException;

	void deleteBidParticipationAppl(User user, Long bidId) throws IOException;

	void deleteBidFile(User user, Long fileId) throws IOException;

	void deleteBidFiles(User user, Long bidId, List<Long> ids) throws IOException;

	boolean isBidReportFileId(Long fileId);

	boolean isBidParticipationFileId(Long fileId);

	void deleteBidAllFiles(User user, Long bidId) throws IOException;
}
