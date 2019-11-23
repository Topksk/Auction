package com.bas.auction.neg.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.docfiles.dto.DocFileAttribute;
import com.bas.auction.docfiles.dto.DocFileSignature;
import com.bas.auction.neg.service.NegFileService;
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
public class NegFileServiceImpl implements NegFileService {
    private final Logger logger = LoggerFactory.getLogger(NegFileServiceImpl.class);
    private final DocFileDAO docFileDAO;
    private final DocFileAttributeDAO docFileAttributeDAO;
    private final DocFileSignatureDAO docFileSignatureDAO;

    @Autowired
    public NegFileServiceImpl(DocFileDAO docFileDAO, DocFileAttributeDAO docFileAttributeDAO, DocFileSignatureDAO docFileSignatureDAO) {
        this.docFileDAO = docFileDAO;
        this.docFileAttributeDAO = docFileAttributeDAO;
        this.docFileSignatureDAO = docFileSignatureDAO;
    }

    @Override
    public List<DocFile> findNegFiles(User user, Long negId) {
        if (user == null)
            return null;
        return docFileDAO.findByAttr(user, "neg_id", negId);
    }

    @Override
    public boolean containsNegPublishReportFileId(List<Long> fileIds) {
        for (Long id : fileIds) {
            List<DocFileAttribute> fa = docFileAttributeDAO.findFileAttributes(id);
            boolean isNegPublishReport = fa.stream().anyMatch(DocFileAttribute::isNegPublishReport);
            if (isNegPublishReport)
                return true;
        }
        return false;
    }

    @Override
    public boolean containsNegResumeReportFileId(List<Long> fileIds) {
        for (Long id : fileIds) {
            List<DocFileAttribute> fa = docFileAttributeDAO.findFileAttributes(id);
            boolean isNegResumeReport = fa.stream().anyMatch(DocFileAttribute::isNegResumeReport);
            if (isNegResumeReport)
                return true;
        }
        return false;
    }

    @Override
    public boolean findIsPublishReportExists(Long negId) {
        return docFileDAO.findIsFileExistsByAttribute("neg_id", String.valueOf(negId), "file_type", "NEG_PUBLISH_REPORT");
    }

    @Override
    public boolean findIsResumeReportExists(Long negId) {
        return docFileDAO.findIsFileExistsByAttribute("neg_id", String.valueOf(negId), "file_type", "NEG_RESUME_REPORT");
    }

    @Override
    public Long findNegCustomerRulesFileId(Long customerId) {
        List<Long> fileIdsByAttribute = docFileDAO.findFileIdsByAttribute("customer_id", String.valueOf(customerId), "file_type", "AUCTION_RULES");
        if (!fileIdsByAttribute.isEmpty())
            return fileIdsByAttribute.get(0);
        return null;
    }

    @Override
    public boolean findIsUnsignedFilesExists(Long negId) {
        return docFileDAO.findIsUnsignedFileExistsByAttribute("neg_id", String.valueOf(negId));
    }

    @Override
    public void makeNegFilesReadOnly(Long userId, Long negId) {
        logger.debug("make neg files read only: negId = {}", negId);
        docFileAttributeDAO.create(userId, "neg_id", negId, "read_only", "Y");
    }

    @Override
    public void makeNegFilesCustomerSignOnly(Long userId, Long negId) {
        logger.debug("make neg files customer sign only: negId = {}", negId);
        docFileAttributeDAO.create(userId, "neg_id", negId, "customer_sign_only", "Y");
    }

    @Override
    public void makeNegFileCustomerReadOnly(Long userId, Long fileId) {
        logger.debug("make neg file customer read only: fileId = {}", fileId);
        docFileAttributeDAO.create(userId, fileId, "customer_only", "Y");
    }

    @Override
    public void makeNegPublishReportPublicAccessible(Long negId) throws IOException {
        logger.debug("make neg file publish public accessible: negId = {}", negId);
        docFileDAO.createSymlinksForFilesByAttr("neg_id", String.valueOf(negId), "file_type", "NEG_PUBLISH_REPORT");
    }

    @Override
    public void makeNegResumeReportPublicAccessible(Long negId) throws IOException {
        logger.debug("make neg file publish resume public accessible: negId = {}", negId);
        docFileDAO.createSymlinksForFilesByAttr("neg_id", String.valueOf(negId), "file_type", "NEG_RESUME_REPORT");
    }

    @Override
    public void makeNegFileNotForIntegration(Long userId, Long fileId) {
        logger.debug("make neg file not for integration: fileId = {}", fileId);
        docFileAttributeDAO.create(userId, fileId, "integration", "N");
    }

    @Override
    public void deleteNegFiles(User user, Long negId) throws IOException {
        logger.debug("delete neg files: negId = {}", negId);
        docFileDAO.deleteByAttr(user, "neg_id", negId, true);
    }

    @Override
    @SpringTransactional
    public void copyNegFiles(User user, Long sourceNegId, Long destinationNegId,
                             boolean copyProtocols) throws IOException {
        logger.debug("copy neg files: negId = {}", sourceNegId);
        List<DocFile> negFiles;
        if (copyProtocols)
            negFiles = findNegFiles(user, sourceNegId);
        else
            negFiles = docFileDAO.findByAttrWithoutProtocols("neg_id", String.valueOf(sourceNegId));
        List<Entry<Long, Long>> sourceDestinationFileIds = negFiles.stream()
                .map(negFile -> copyFile(user.getUserId(), negFile))
                .collect(toList());
        List<DocFileAttribute> fileAttributes = sourceDestinationFileIds.stream()
                .map(entry -> findFileAttributesForCopy(user, destinationNegId, entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .collect(toList());
        List<DocFileSignature> fileSignatures = sourceDestinationFileIds.stream()
                .map(entry -> findFileSignaturesForCopy(entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .collect(toList());
        docFileAttributeDAO.create(fileAttributes);
        docFileSignatureDAO.create(fileSignatures);
        physicallyCopyFilesForNeg(destinationNegId);
    }

    protected Entry<Long, Long> copyFile(Long userId, DocFile negFile) {
        negFile.setCreatedBy(userId);
        long sourceFileId = negFile.getFileId();
        negFile.setCopiedFileId(sourceFileId);
        Long newFileId = docFileDAO.create(negFile);
        return new SimpleEntry<>(sourceFileId, newFileId);
    }

    protected List<DocFileAttribute> findFileAttributesForCopy(User user, Long newNegId, Long sourceFileId, Long destinationFileId) {
        Predicate<DocFileAttribute> isReadOnly = DocFileAttribute::isReadOnly;
        List<DocFileAttribute> fileAttributes = docFileAttributeDAO.findFileAttributes(sourceFileId);
        fileAttributes = fileAttributes.stream()
                .filter(isReadOnly.negate())
                .collect(toList());
        fileAttributes.forEach(attr -> {
            attr.setFileId(destinationFileId);
            attr.setCreatedBy(user.getUserId());
        });
        setNewNegIdAttribute(newNegId, fileAttributes);
        setStage1ProtocolAttribute(fileAttributes);
        return fileAttributes;
    }

    protected void setNewNegIdAttribute(Long newNegId, List<DocFileAttribute> fileAttributes) {
        String value = String.valueOf(newNegId);
        fileAttributes.stream()
                .filter(DocFileAttribute::isNegId)
                .forEach(a -> a.setValue(value));
    }

    protected void setStage1ProtocolAttribute(List<DocFileAttribute> fileAttributes) {
        String suffix = "_STAGE1";
        fileAttributes.stream()
                .filter(DocFileAttribute::isNegProtocol)
                .forEach(a -> a.setValue(a.getValue() + suffix));
    }

    protected List<DocFileSignature> findFileSignaturesForCopy(Long sourceFileId, Long destinationFileId) {
        List<DocFileSignature> fileSignatures = docFileSignatureDAO.findFileSignatures(sourceFileId);
        fileSignatures.forEach(fs -> fs.setFileId(destinationFileId));
        return fileSignatures;
    }

    protected void physicallyCopyFilesForNeg(Long destinationNegId) throws IOException {
        logger.debug("physically copy files: negId = {}", destinationNegId);
        docFileDAO.physicallyCopyFilesByAttr("neg_id", String.valueOf(destinationNegId));
    }

    @Override
    public boolean deleteNegFile(User user, Long fileId) throws IOException {
        logger.debug("delete neg file: fileId = {}", fileId);
        return docFileDAO.delete(user, fileId);
    }

    @Override
    public void deleteNegFilesCustomerOnlyAttr(Long negId) {
        logger.debug("delete neg files customer only attr: negId = {}", negId);
        docFileAttributeDAO.delete("neg_id", negId, "customer_only", "Y");
    }

    @Override
    public void deleteNegFilesReadOnlyAttr(Long negId) {
        logger.debug("delete neg files read only attr: negId = {}", negId);
        docFileAttributeDAO.delete("neg_id", negId, "read_only", "Y");
    }

    @Override
    public void deleteUnlockReport(User user, Long negId) throws IOException {
        logger.debug("delete unlock report: negId = {}", negId);
        docFileDAO.deleteByAttr(user, "neg_id", negId, "file_type", "NEG_OPENING_REPORT");
    }

    @Override
    public void deletePublishReport(User user, Long negId) throws IOException {
        logger.debug("delete publish report: negId = {}", negId);
        docFileDAO.deleteByAttr(user, "neg_id", negId, "file_type", "NEG_PUBLISH_REPORT");
    }

    @Override
    public void deleteResumeReport(User user, Long negId) throws IOException {
        logger.debug("delete resume report: negId = {}", negId);
        docFileDAO.deleteByAttr(user, "neg_id", negId, "file_type", "NEG_RESUME_REPORT");
    }
}
