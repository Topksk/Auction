package com.bas.auction.neg.draft.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegTeamDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.draft.service.CantDeleteSecondStageException;
import com.bas.auction.neg.draft.service.CantUpdateNotDraftNegException;
import com.bas.auction.neg.draft.service.NegDraftService;
import com.bas.auction.neg.draft.service.OnlyDraftNegCanBeDeletedException;
import com.bas.auction.neg.dto.NegTeam;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.service.NegFileService;
import com.bas.auction.neg.setting.service.NegSettingService;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class NegDraftServiceImpl implements NegDraftService {
    private final Logger logger = LoggerFactory.getLogger(NegDraftServiceImpl.class);
    private final NegotiationDAO negDAO;
    private final CustomerSettingDAO customerSettingsDAO;
    private final NegTeamDAO negTeamDAO;
    private final NegLineDAO negLineDAO;
    private final NegFileService negFileService;
    private final NegSettingService negSettingService;

    @Autowired
    public NegDraftServiceImpl(NegotiationDAO negDAO, CustomerSettingDAO customerSettingsDAO, NegTeamDAO negTeamDAO,
                               NegLineDAO negLineDAO, NegFileService negFileService, NegSettingService negSettingService) {
        this.negDAO = negDAO;
        this.customerSettingsDAO = customerSettingsDAO;
        this.negTeamDAO = negTeamDAO;
        this.negLineDAO = negLineDAO;
        this.negFileService = negFileService;
        this.negSettingService = negSettingService;
    }

    @Override
    @SpringTransactional
    public Negotiation create(User user, String title, String negType) {
        logger.info("create neg: type = {}, title = {}", negType, title);
        Negotiation neg = createNegHeader(user, title, negType);
        negSettingService.create(neg);
        createOrganizer(user, neg);
        return negDAO.findAndIndexSync(user, neg.getNegId());
    }

    private Negotiation createNegHeader(User user, String title, String negType) {
        Long settingId = customerSettingsDAO.findMainId(user.getCustomerId());
        Negotiation neg = new Negotiation();
        neg.setSettingId(settingId);
        neg.setNegStatus("DRAFT");
        neg.setNegType(NegType.valueOf(negType));
        neg.setTitle(title);
        neg.setTitleKz(title);
        neg.setCustomerId(user.getCustomerId());
        neg.setCreatedBy(user.getUserId());
        neg.setLastUpdatedBy(user.getLastUpdatedBy());
        if (neg.isTender2()) {
            neg.setStage(1);
        }
        return negDAO.insert(neg);
    }

    private NegTeam createOrganizer(User user, Negotiation neg) {
        NegTeam organizer = new NegTeam();
        organizer.setNegId(neg.getNegId());
        organizer.setUserId(user.getUserId());
        organizer.setRoleCode("005");
        organizer.setMemberPosition(user.getUserPosition());
        return negTeamDAO.create(user, organizer);
    }

    @Override
    @SpringTransactional
    public Negotiation copyNeg(User user, Long negId, String docNumber, Integer stage,
                               boolean copyProtocols) throws IOException {
        Long newNegId = negDAO.copyNeg(user, negId, docNumber, "DRAFT", stage);
        negSettingService.copyNegSettings(user, negId, newNegId);
        negLineDAO.copyNotFailedNegLines(user, negId, newNegId);
        negTeamDAO.copyTeam(user, negId, newNegId);
        negFileService.copyNegFiles(user, negId, newNegId, copyProtocols);
        return negDAO.findAndIndexSync(user, newNegId);
    }

    @Override
    @SpringTransactional
    public Negotiation update(User user, Negotiation neg) {
        logger.info("update neg: {}", neg.getNegId());
        Long negId = neg.getNegId();
        validateUpdate(negId);
        neg.setUnlockDate(neg.getCloseDate());
        neg.setActualCloseDate(neg.getCloseDate());
        negDAO.update(user, neg);
        negTeamDAO.upsert(user, negId, neg.getNegTeam());
        negTeamDAO.delete(negId, neg.getDelTeam());
        negLineDAO.delete(negId, neg.getDelLines());
        negLineDAO.create(user, negId, neg.getNewLines());
        return negDAO.findAndUpdateIndexAsync(user, negId);
    }

    private void validateUpdate(Long negId) {
        boolean isPublishReportExists = negFileService.findIsPublishReportExists(negId);
        if (isPublishReportExists)
            throw new CantUpdateNotDraftNegException();
        boolean isDraft = negDAO.findIsDraft(negId);
        if (!isDraft)
            throw new CantUpdateNotDraftNegException();
    }

    @Override
    @SpringTransactional
    public void delete(User user, Long negId) throws IOException {
        logger.info("delete neg: {}", negId);
        validateDelete(negId);
        negTeamDAO.deleteNegTeam(negId);
        negLineDAO.deleteNegLines(negId);
        negFileService.deleteNegFiles(user, negId);
        negSettingService.delete(negId);
        negDAO.delete(negId);
        negDAO.deleteFromSearchIndex(negId);
    }

    private void validateDelete(Long negId) {
        boolean isDraft = negDAO.findIsDraft(negId);
        if (!isDraft)
            throw new OnlyDraftNegCanBeDeletedException();
        boolean isTender2Stage2 = negDAO.findIsTender2Stage2(negId);
        if (isTender2Stage2) {
            throw new CantDeleteSecondStageException();
        }
    }

    @Override
    @SpringTransactional
    public Negotiation deleteFiles(User user, Long negId, List<Long> fileIds) throws IOException {
        boolean containsNegPublishReportFileId = negFileService.containsNegPublishReportFileId(fileIds);
        boolean containsNegResumeReportFileId = negFileService.containsNegResumeReportFileId(fileIds);
        if (containsNegResumeReportFileId) {
            boolean isTender2Stage1 = negDAO.findIsTender2Stage1(negId);
            if (isTender2Stage1) {
                negFileService.deleteResumeReport(user, negId);
                return negDAO.findAndUpdateIndexAsync(user, negId);
            }
        }
        for (Long id : fileIds) {
            boolean success = negFileService.deleteNegFile(user, id);
            if(!success)
                throw new CantUpdateNotDraftNegException();
        }
        if (containsNegPublishReportFileId)
            negFileService.deleteNegFilesReadOnlyAttr(negId);
        return negDAO.findAndUpdateIndexAsync(user, negId);
    }
}