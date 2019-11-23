package com.bas.auction.neg.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.docfiles.dto.DocFile;

import java.io.IOException;
import java.util.List;

public interface NegFileService {
	List<DocFile> findNegFiles(User user, Long negId);

	boolean containsNegResumeReportFileId(List<Long> fileIds);

    boolean containsNegPublishReportFileId(List<Long> fileIds);

	boolean findIsPublishReportExists(Long negId);

	boolean findIsResumeReportExists(Long negId);

	Long findNegCustomerRulesFileId(Long customerId);

	boolean findIsUnsignedFilesExists(Long negId);

    void makeNegFilesReadOnly(Long userId, Long negId);

	void makeNegFilesCustomerSignOnly(Long userId, Long negId);

	void makeNegFileCustomerReadOnly(Long userId, Long fileId);

	void makeNegPublishReportPublicAccessible(Long negId) throws IOException;

	void makeNegResumeReportPublicAccessible(Long negId) throws IOException;

	void deleteUnlockReport(User user, Long negId) throws IOException;

	void deletePublishReport(User user, Long negId) throws IOException;

	void deleteResumeReport(User user, Long negId) throws IOException;

	boolean deleteNegFile(User user, Long id) throws IOException;

	void makeNegFileNotForIntegration(Long userId, Long fileId);

	void deleteNegFiles(User user, Long negId) throws IOException;

	void deleteNegFilesReadOnlyAttr(Long negId);

	void deleteNegFilesCustomerOnlyAttr(Long negId);

	void copyNegFiles(User user, Long negId, Long newNegId, boolean copyProtocols) throws IOException;
}
