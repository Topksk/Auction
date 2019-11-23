package com.bas.auction.neg.publish.service.impl;

import com.bas.auction.core.Conf;
import com.bas.auction.docfiles.dao.AllDocFilesShouldBeSignedException;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegTeamDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.publish.service.*;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.neg.setting.service.NegSettingService;
import com.bas.auction.workday.service.WorkdayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class NegPublishValidationServiceImpl implements NegPublishValidationService {
    private final Logger logger = LoggerFactory.getLogger(NegPublishValidationServiceImpl.class);
    private final BigDecimal MAX_DUMPING_THRESHOLD = new BigDecimal("70");
    private final BigDecimal NINETY_NINE = new BigDecimal("99");
    private final NegotiationDAO negDAO;
    private final NegTeamDAO negTeamDAO;
    private final NegLineDAO negLineDAO;
    private final NegFileService negFileService;
    private final NegSettingService negSettingService;
    private final WorkdayService workdayService;
    private final Conf conf;

    @Autowired
    public NegPublishValidationServiceImpl(NegotiationDAO negDAO, NegTeamDAO negTeamDAO, NegLineDAO negLineDAO,
                                           NegFileService negFileService, NegSettingService negSettingService,
                                           WorkdayService workdayService, Conf conf) {
        this.negDAO = negDAO;
        this.negTeamDAO = negTeamDAO;
        this.negLineDAO = negLineDAO;
        this.negFileService = negFileService;
        this.negSettingService = negSettingService;
        this.workdayService = workdayService;
        this.conf = conf;
    }

    @Override
    public void validatePublish(Long negId) {
        logger.debug("validating publish: negId={}", negId);
        validateNegStatus(negId);
        validatePublishReportExistence(negId);
        validatePublishReport(negId);
    }

    @Override
    public void validatePublishReport(Long negId) {
        logger.debug("validating publish report: negId={}", negId);
        validateOrganizer(negId);
        validateFiles(negId);
        if (conf.isNegPublishPeriodValidationEnabled())
            validateDates(negId);
        NegType negType = negDAO.findNegType(negId);
        if (negType == NegType.AUCTION)
            validateAuction(negId);
        else if (negType == NegType.TENDER || negType == NegType.TENDER2)
            validateDumping(negId);
    }

    private void validateDumping(Long negId) {
        Negotiation neg = negDAO.findDumpingDataForPublishValidation(negId);
        if (!neg.isDumpingControlEnabled())
            return;
        if (neg.getGoodDumpingCalcMethod() != null && neg.getGoodDumpingThreshold() == null)
            throw new GoodDumpingThresholdRequiredException();
        if (neg.getGoodDumpingCalcMethod() == null && neg.getGoodDumpingThreshold() != null)
            throw new GoodDumpingCalcMethodRequiredException();
        if (neg.getWorkDumpingCalcMethod() != null && neg.getWorkDumpingThreshold() == null)
            throw new WorkDumpingThresholdRequiredException();
        if (neg.getWorkDumpingCalcMethod() == null && neg.getWorkDumpingThreshold() != null)
            throw new WorkDumpingCalcMethodRequiredException();
        if (neg.getServiceDumpingCalcMethod() != null && neg.getServiceDumpingThreshold() == null)
            throw new ServiceDumpingThresholdRequiredException();
        if (neg.getServiceDumpingCalcMethod() == null && neg.getServiceDumpingThreshold() != null)
            throw new ServiceDumpingCalcMethodRequiredException();
        validateDumpingThreshold(neg.getGoodDumpingThreshold());
        validateDumpingThreshold(neg.getWorkDumpingThreshold());
        validateDumpingThreshold(neg.getServiceDumpingThreshold());
    }

    private void validateDumpingThreshold(BigDecimal dumpingThreshold) {
        if (dumpingThreshold == null)
            return;
        boolean notInRange = BigDecimal.ZERO.compareTo(dumpingThreshold) >= 0
                || MAX_DUMPING_THRESHOLD.compareTo(dumpingThreshold) < 0;
        if (notInRange) {
            logger.error("dumping threshold {}", dumpingThreshold);
            throw new DumpingThresholdNotInRangeException();
        }
    }

    protected void validateNegStatus(Long negId) {
        boolean isDraft = negDAO.findIsDraft(negId);
        if (!isDraft)
            throw new NegAlreadyPublishedException();
    }

    private void validatePublishReportExistence(Long negId) {
        boolean isPublishReportExists = negFileService.findIsPublishReportExists(negId);
        if (!isPublishReportExists)
            throw new NegPublishReportRequiredException();
    }

    private void validateFiles(Long negId) {
        boolean isUnsignedFilesExists = negFileService.findIsUnsignedFilesExists(negId);
        if (isUnsignedFilesExists)
            throw new AllDocFilesShouldBeSignedException();
    }

    protected void validateOrganizer(Long negId) {
        boolean isNegOwnerOrganizer = negTeamDAO.findIsNegOwnerOrganizer(negId);
        if (!isNegOwnerOrganizer)
            throw new NegCreatorMustBeOrganizerException();
    }

    protected void validateDates(Long negId) {
        Negotiation neg = negDAO.findForPublishValidation(negId);
        if (!workdayService.isWorkingDay(neg.getOpenDate(), 9))
            throw new NegOpenDateIsNotWorkingDayException();
        if (!workdayService.isWorkingDay(neg.getCloseDate(), 10))
            throw new NegCloseDateIsNotWorkingDayException();
    }

    protected void validateAuction(Long negId) {
        Negotiation neg = negDAO.findAuctionDataForPublishValidation(negId);
        validateAuctionBidStep(neg);
        validateAuctionDuration(neg);
    }

    protected void validateAuctionBidStep(Negotiation neg) {
        BigDecimal auctionBidStep = neg.getAuctionBidStep();
        if (auctionBidStep == null)
            throw new AuctionBidStepReqiuredException();
        if (auctionBidStep.doubleValue() < 0)
            throw new NegativeAuctionBidStepException();
        if (auctionBidStep.compareTo(BigDecimal.ZERO) == 0)
            throw new ZeroAuctionBidStepException();
        if ("PERCENT".equals(neg.getAuctionBidStepType()))
            validateAuctionBidStepPercentRange(auctionBidStep);
        else
            validateAuctionBidStepSumMinValue(neg.getNegId(), auctionBidStep);
    }

    protected void validateAuctionBidStepPercentRange(BigDecimal auctionBidStep) {
        if (auctionBidStep.compareTo(BigDecimal.ONE) < 0 ||
                auctionBidStep.compareTo(NINETY_NINE) > 0)
            throw new AuctionBidStepPercentNotInRangeException();
    }

    protected void validateAuctionBidStepSumMinValue(Long negId, BigDecimal auctionBidStep) {
        BigDecimal negLinesMinUnitPrice = negLineDAO.findNegLinesMinUnitPrice(negId);
        if (auctionBidStep.compareTo(negLinesMinUnitPrice) > 0)
            throw new AuctionBidStepIsGreaterThanMinLineUnitPriceException();
    }

    protected void validateAuctionDuration(Negotiation neg) {
        int auctionDuration = negSettingService.findAuctionDuration(neg.getNegId());
        int days = getPublishedPeriodInDays(neg);
        if (days < auctionDuration) {
            logger.error("auction day diff {}", days);
            throw new ShortAuctionDurationException(auctionDuration);
        }
    }

    protected int getPublishedPeriodInDays(Negotiation neg) {
        long dateDiff = neg.getCloseDate().getTime() - neg.getOpenDate().getTime();
        return (int) TimeUnit.DAYS.convert(dateDiff, TimeUnit.MILLISECONDS);
    }
}
