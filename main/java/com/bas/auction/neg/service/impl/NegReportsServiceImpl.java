package com.bas.auction.neg.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.reports.ReportService;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.service.*;
import oracle.apps.xdo.XDOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NegReportsServiceImpl implements NegReportsService {
    private final Logger logger = LoggerFactory.getLogger(NegReportsServiceImpl.class);
    private final ReportService reportService;
    private final NegFileService negFileService;

    @Autowired
    public NegReportsServiceImpl(NegFileService negFileService, ReportService reportService) {
        this.negFileService = negFileService;
        this.reportService = reportService;
    }

    private Map<String, String> getPublishReportFileAttributes(Long negId) {
        Map<String, String> reportFileAttributes = new HashMap<>();
        reportFileAttributes.put("neg_id", String.valueOf(negId));
        reportFileAttributes.put("file_type", "NEG_PUBLISH_REPORT");
        return reportFileAttributes;
    }

    private Map<String, Object[]> getPublishReportParameters(Long userId, Long negId) {
        Map<String, Object[]> reportParameters = new HashMap<>();
        Object[] p = new Object[]{negId};
        reportParameters.put("NEG_HEADER", new Object[]{userId, negId});
        reportParameters.put("NEG_LINES", p);
        reportParameters.put("DOCUMENTS", p);
        return reportParameters;
    }

    @Override
    public void generatePublishReport(User user, Long negId) {
        logger.debug("generating publish report: {}", negId);
        try {
            negFileService.deletePublishReport(user, negId);
            negFileService.makeNegFilesReadOnly(user.getUserId(), negId);
            Map<String, String> reportFileAttributes = getPublishReportFileAttributes(negId);
            Map<String, Object[]> reportParameters = getPublishReportParameters(user.getUserId(), negId);
            reportService.generateReport(user.getUserId(), "NEG_PUBLISH_REPORT", reportFileAttributes, reportParameters);
        } catch (IOException | NoSuchAlgorithmException | XPathExpressionException | SQLException
                | ParserConfigurationException | TransformerException | XDOException | URISyntaxException e) {
            logger.error("Error generating publish report: negId={}", negId, e);
            throw new PublishReportGeneratingException();
        }
    }

    @Override
    public void generateUnlockReport(User user, Long negId) {
        logger.debug("generating unlock report: {}", negId);
        try {
            negFileService.deleteUnlockReport(user, negId);
            Map<String, String> reportFileAttributes = new HashMap<>();
            reportFileAttributes.put("neg_id", String.valueOf(negId));
            reportFileAttributes.put("file_type", "NEG_OPENING_REPORT");
            Map<String, Object[]> reportParameters = new HashMap<>();
            Object[] p = new Object[]{negId};
            reportParameters.put("NEG_HEADER", p);
            reportParameters.put("NEG_LINES", p);
            reportParameters.put("NEG_LINE_BIDS", p);
            reportParameters.put("MEMBERS", p);
            reportParameters.put("BID_DOCUMENTS", p);
            reportService.generateReport(user.getUserId(), "NEG_OPENING_REPORT", reportFileAttributes, reportParameters);
            negFileService.makeNegFilesCustomerSignOnly(user.getUserId(), negId);
        } catch (IOException | NoSuchAlgorithmException | XPathExpressionException | SQLException
                | ParserConfigurationException | TransformerException | XDOException | URISyntaxException e) {
            logger.error("Error generating unlock report: negId={}", negId, e);
            throw new UnlockReportGeneratingException();
        }
    }

    private Map<String, String> getResumeReportFileAttributes(Long negId) {
        Map<String, String> reportFileAttributes = new HashMap<>();
        reportFileAttributes.put("neg_id", String.valueOf(negId));
        reportFileAttributes.put("file_type", "NEG_RESUME_REPORT");
        return reportFileAttributes;
    }

    private Map<String, Object[]> getResumeReportParameters(NegType negType, Long negId) {
        Map<String, Object[]> reportParameters = new HashMap<>();
        Object[] p = new Object[]{negId};
        reportParameters.put("NEG_HEADER", p);
        reportParameters.put("NEG_LINES", p);
        reportParameters.put("MEMBERS", p);
        if (negType == NegType.RFQ || negType == NegType.AUCTION) {
            reportParameters.put("BIDS", p);
            reportParameters.put("REJECTS", p);
            reportParameters.put("REJECT_REASONS", p);
        } else if (negType == NegType.TENDER || negType == NegType.TENDER2) {
            reportParameters.put("NEG_LINE_BIDS", p);
            reportParameters.put("DISCOUNTS", p);
        }
        return reportParameters;
    }

    private String getResumeReportCode(NegType negType, Optional<Integer> stage) {
        if (negType == NegType.RFQ) {
            return "RFQ_RESUME_REPORT";
        } else if (negType == NegType.AUCTION) {
            return "AUCTION_RESUME_REPORT";
        } else if (negType == NegType.TENDER) {
            return "TENDER_RESUME_REPORT";
        } else if (negType == NegType.TENDER2 && stage.orElse(0) == 1) {
            return "TENDER2_STAGE1_RESUME_REPORT";
        } else if (negType == NegType.TENDER2 && stage.orElse(0) == 2) {
            return "TENDER2_STAGE2_RESUME_REPORT";
        }
        return null;
    }

    private String getResumeReportCode(NegType negType) {
        return getResumeReportCode(negType, Optional.empty());
    }

    private long generateResumeReport(User user, Long negId, NegType negType, String repCode) throws Exception {
        logger.debug("generating resume report: {}", negId);
        Map<String, String> reportFileAttributes = getResumeReportFileAttributes(negId);
        Map<String, Object[]> reportParameters = getResumeReportParameters(negType, negId);
        long fileId = reportService.generateReport(user.getUserId(), repCode, reportFileAttributes, reportParameters);
        negFileService.makeNegFileCustomerReadOnly(user.getUserId(), fileId);
        return fileId;
    }

    private void generateResumeReport(User user, Long negId, NegType negType) throws Exception {
        String repCode = getResumeReportCode(negType);
        generateResumeReport(user, negId, negType, repCode);
    }

    @Override
    public void generateRfqResumeReport(User user, Long negId) {
        try {
            negFileService.deleteResumeReport(user, negId);
            generateResumeReport(user, negId, NegType.RFQ);
        } catch (Exception e) {
            logger.error("Error generating rfq resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }
    }

    @Override
    public void generateAuctionResumeReport(User user, Long negId) {
        try {
            negFileService.deleteResumeReport(user, negId);
            generateResumeReport(user, negId, NegType.AUCTION);
        } catch (Exception e) {
            logger.error("Error generating auction resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }
    }

    @Override
    public void generateTenderResumeReport(User user, Long negId) {
        try {
            negFileService.deleteResumeReport(user, negId);
            generateResumeReport(user, negId, NegType.TENDER);
        } catch (Exception e) {
            logger.error("Error generating tender resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }
    }

    @Override
    public void generateTender2Stage1ResumeReport(User user, Long negId) {
        try {
            negFileService.deleteResumeReport(user, negId);
            String repCode = getResumeReportCode(NegType.TENDER2, Optional.of(1));
            long fileId = generateResumeReport(user, negId, NegType.TENDER2, repCode);
            negFileService.makeNegFileNotForIntegration(user.getUserId(), fileId);
        } catch (Exception e) {
            logger.error("Error generating tender2 stage 1 resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }
    }

    @Override
    public void generateTender2Stage2ResumeReport(User user, Long negId) {
        try {
            negFileService.deleteResumeReport(user, negId);
            String repCode = getResumeReportCode(NegType.TENDER2, Optional.of(2));
            generateResumeReport(user, negId, NegType.TENDER2, repCode);
        } catch (Exception e) {
            logger.error("Error generating tender2 stage 1 resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }
    }

    @Override
    public void generateFailedTender2Stage2ResumeReport(User user, Long negId) {
        try {
            String repCode = getResumeReportCode(NegType.TENDER2, Optional.of(2));
            generateResumeReport(user, negId, NegType.TENDER2, repCode);
        } catch (Exception e) {
            logger.error("Error generating tender2 stage 2 resume report: negId={}", negId, e);
            throw new ResumeReportGeneratingException();
        }

    }

}
