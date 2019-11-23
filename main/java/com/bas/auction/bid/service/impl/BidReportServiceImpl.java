package com.bas.auction.bid.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.bid.service.BidReportService;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.reports.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Service
public class BidReportServiceImpl implements BidReportService {
    private final BidFileService bidFileService;
    private final ReportService reportService;
    private final MessageDAO messageDAO;

    @Autowired
    public BidReportServiceImpl(BidFileService bidFileService, ReportService reportService, MessageDAO messageDAO) {
        this.bidFileService = bidFileService;
        this.reportService = reportService;
        this.messageDAO = messageDAO;
    }

    private Map<String, String> getBidReportFileAttributes(Long bidId) {
        Map<String, String> reportFileAttributes = new HashMap<>();
        reportFileAttributes.put("bid_id", String.valueOf(bidId));
        reportFileAttributes.put("file_type", "BID_REPORT");
        return reportFileAttributes;
    }

    private Map<String, Object[]> getBidReportParameters(Long bidId) {
        Map<String, Object[]> reportParameters = new HashMap<>();
        Object[] reportParameterValue = new Object[]{bidId};
        reportParameters.put("BID_HEADER", reportParameterValue);
        reportParameters.put("BID_LINES", reportParameterValue);
        reportParameters.put("DOCUMENTS", reportParameterValue);
        return reportParameters;
    }

    @Override
    public void generateBidReport(User user, Long bidId) throws Exception {
        bidFileService.deleteBidReport(user, bidId);
        Map<String, String> reportFileAttributes = getBidReportFileAttributes(bidId);
        Map<String, Object[]> reportParameters = getBidReportParameters(bidId);
        String reportName = messageDAO.get("BID_REPORT", singletonMap("bid_id", bidId.toString()));
        reportService.generateReport(user.getUserId(), reportName, "BID_REPORT", reportFileAttributes, reportParameters);
    }

    private Map<String, String> getBidParticipationApplFileAttributes(Long bidId) {
        Map<String, String> reportFileAttributes = new HashMap<>();
        reportFileAttributes.put("bid_id", String.valueOf(bidId));
        reportFileAttributes.put("file_type", "BID_PARTICIPATION_APPL");
        return reportFileAttributes;
    }

    private Map<String, Object[]> getBidParticipationApplReportParameters(Long bidId) {
        Map<String, Object[]> reportParameters = new HashMap<>();
        Object[] reportParameterValue = new Object[]{bidId};
        reportParameters.put("REP_HEADER", reportParameterValue);
        reportParameters.put("ITEMS", reportParameterValue);
        reportParameters.put("DISCOUNTS", new Object[]{bidId, bidId});
        reportParameters.put("DOCUMENTS", reportParameterValue);
        return reportParameters;
    }

    @Override
    public void generateBidParticipationAppl(User user, Long bidId) throws Exception {
        bidFileService.deleteBidParticipationAppl(user, bidId);
        Map<String, String> reportFileAttributes = getBidParticipationApplFileAttributes(bidId);
        Map<String, Object[]> reportParameters = getBidParticipationApplReportParameters(bidId);
        reportService.generateReport(user.getUserId(), "BID_PARTICIPATION_APPL", reportFileAttributes, reportParameters);
    }

}
