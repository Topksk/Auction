package com.bas.auction.bid.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.docfiles.dto.DocFileAttribute;
import com.bas.auction.docfiles.dto.DocFileSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Service
public class BidFileServiceImpl implements BidFileService {
    private final Logger logger = LoggerFactory.getLogger(BidFileServiceImpl.class);
    private final DocFileDAO docFileDAO;
    private final DocFileAttributeDAO docFileAttributeDAO;
    private final DocFileSignatureDAO docFileSignatureDAO;

    @Autowired
    public BidFileServiceImpl(DocFileDAO docFileDAO, DocFileAttributeDAO docFileAttributeDAO, DocFileSignatureDAO docFileSignatureDAO) {
        this.docFileDAO = docFileDAO;
        this.docFileAttributeDAO = docFileAttributeDAO;
        this.docFileSignatureDAO = docFileSignatureDAO;
    }

    @Override
    public void makeBidFilesReadOnly(Long userId, Long bidId) {
        docFileAttributeDAO.create(userId, "bid_id", bidId, "read_only", "Y");
    }

    @Override
    public void deleteBidReport(User user, Long bidId) throws IOException {
        docFileDAO.deleteByAttr(user, "bid_id", bidId, "file_type", "BID_REPORT");
    }

    @Override
    public void deleteBidParticipationAppl(User user, Long bidId) throws IOException {
        docFileDAO.deleteByAttr(user, "bid_id", bidId, "file_type", "BID_PARTICIPATION_APPL");
    }

    @Override
    public void deleteBidFiles(User user, Long bidId, List<Long> ids) throws IOException {
        boolean containsBidReportFileId = containsBidReportFileId(ids);
        boolean containsBidParticipationFileId = false;
        if (!containsBidReportFileId)
            containsBidParticipationFileId = containsBidParticipationFileId(ids);
        for (Long id : ids) {
            deleteBidFile(user, id);
        }
        if (containsBidReportFileId || containsBidParticipationFileId)
            deleteBidFilesReadOnlyAttr(bidId);
    }

    @Override
    public void deleteBidFile(User user, Long id) throws IOException {
        docFileDAO.delete(user, id);
    }

    @Override
    public void deleteBidFilesReadOnlyAttr(Long bidId) {
        docFileAttributeDAO.delete("bid_id", bidId, "read_only", "Y");
    }

    private boolean containsBidReportFileId(List<Long> fileIds) {
        return fileIds.stream()
                .anyMatch(this::isBidReportFileId);
    }

    private boolean containsBidParticipationFileId(List<Long> fileIds) {
        return fileIds.stream()
                .anyMatch(this::isBidParticipationFileId);
    }

    @Override
    public boolean isBidReportFileId(Long fileId) {
        List<DocFileAttribute> fileAttributes = docFileAttributeDAO.findFileAttributes(fileId);
        return fileAttributes.stream().anyMatch(DocFileAttribute::isBidReport);
    }

    @Override
    public boolean isBidParticipationFileId(Long fileId) {
        List<DocFileAttribute> fileAttributes = docFileAttributeDAO.findFileAttributes(fileId);
        return fileAttributes.stream().anyMatch(DocFileAttribute::isBidParticipationReport);
    }

    @Override
    public void deleteBidAllFiles(User user, Long bidId) throws IOException {
        docFileDAO.deleteByAttr(user, "bid_id", bidId, true);
    }

    @Override
    public List<DocFile> findBidFiles(User user, Long bidId) {
        return docFileDAO.findByAttr(user, "bid_id", bidId);
    }

    @Override
    public boolean findIsUnsignedFilesExists(Long bidId) {
        return docFileDAO.findIsUnsignedFileExistsByAttribute("bid_id", String.valueOf(bidId));
    }

    @Override
    public boolean findIsBidReportExists(Long bidId) {
        return docFileDAO.findIsFileExistsByAttribute("bid_id", String.valueOf(bidId), "file_type", "BID_REPORT");
    }

    @Override
    public boolean findIsBidParticipationReportExists(Long bidId) {
        return docFileDAO.findIsFileExistsByAttribute("bid_id", String.valueOf(bidId), "file_type", "BID_PARTICIPATION_APPL");
    }

    @Override
    public void copyBidFiles(User user, Long sourceBidId, Long destinationBidId) throws IOException {
        logger.debug("copy bid files: bidId = {}", sourceBidId);
        List<DocFile> bidFiles = docFileDAO.findByAttrWithoutProtocols("bid_id", String.valueOf(sourceBidId));
        List<Entry<Long, Long>> sourceDestinationFileIds = bidFiles.stream()
                .map(bidFile -> copyFile(user.getUserId(), bidFile))
                .collect(toList());
        List<DocFileAttribute> fileAttributes = sourceDestinationFileIds.stream()
                .map(entry -> findFileAttributesForCopy(user, destinationBidId, entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .collect(toList());
        List<DocFileSignature> fileSignatures = sourceDestinationFileIds.stream()
                .map(entry -> findFileSignaturesForCopy(entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .collect(toList());
        docFileAttributeDAO.create(fileAttributes);
        docFileSignatureDAO.create(fileSignatures);
        physicallyCopyFilesForBid(destinationBidId);
    }

    protected Entry<Long, Long> copyFile(Long userId, DocFile bidFile) {
        bidFile.setCreatedBy(userId);
        long sourceFileId = bidFile.getFileId();
        bidFile.setCopiedFileId(sourceFileId);
        Long newFileId = docFileDAO.create(bidFile);
        return new SimpleEntry<>(sourceFileId, newFileId);
    }

    protected List<DocFileAttribute> findFileAttributesForCopy(User user, Long newBidId, Long sourceFileId,
                                                               Long destinationFileId) {
        Predicate<DocFileAttribute> isReadOnly = DocFileAttribute::isReadOnly;
        List<DocFileAttribute> fileAttributes = docFileAttributeDAO.findFileAttributes(sourceFileId);
        fileAttributes = fileAttributes.stream()
                .filter(isReadOnly.negate())
                .collect(toList());
        fileAttributes.forEach(attr -> {
            attr.setFileId(destinationFileId);
            attr.setCreatedBy(user.getUserId());
        });
        setNewBidIdAttribute(newBidId, fileAttributes);
        return fileAttributes;
    }

    protected void setNewBidIdAttribute(Long newBidId, List<DocFileAttribute> fileAttributes) {
        String value = String.valueOf(newBidId);
        fileAttributes.stream()
                .filter(DocFileAttribute::isBidId)
                .forEach(a -> a.setValue(value));
    }

    protected List<DocFileSignature> findFileSignaturesForCopy(Long sourceFileId, Long destinationFileId) {
        List<DocFileSignature> fileSignatures = docFileSignatureDAO.findFileSignatures(sourceFileId);
        fileSignatures.forEach(fs -> fs.setFileId(destinationFileId));
        return fileSignatures;
    }

    protected void physicallyCopyFilesForBid(Long destinationBidId) throws IOException {
        docFileDAO.physicallyCopyFilesByAttr("bid_id", String.valueOf(destinationBidId));
    }
}
