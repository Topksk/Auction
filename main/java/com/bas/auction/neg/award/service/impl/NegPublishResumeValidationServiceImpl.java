package com.bas.auction.neg.award.service.impl;

import com.bas.auction.docfiles.dao.AllDocFilesShouldBeSignedException;
import com.bas.auction.neg.award.service.NegPublishResumeValidationService;
import com.bas.auction.neg.award.service.NegResumeAlreadyPublishedException;
import com.bas.auction.neg.award.service.NegResumeReportRequiredException;
import com.bas.auction.neg.award.service.NegVotingShouldBeFinishedException;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.service.NegFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class NegPublishResumeValidationServiceImpl implements NegPublishResumeValidationService {
    private final NegotiationDAO negDAO;
    private final NegFileService negFileService;

    @Autowired
    public NegPublishResumeValidationServiceImpl(NegotiationDAO negDAO, NegFileService negFileService) {
        this.negDAO = negDAO;
        this.negFileService = negFileService;
    }

    @Override
    public void validatePublishResume(Long negId) {
        String negStatus = negDAO.findNegStatus(negId);
        if (isNegResumePublished(negStatus))
            throw new NegResumeAlreadyPublishedException();
        if (!isNegVotingFinished(negStatus))
            throw new NegVotingShouldBeFinishedException();
        if (!negFileService.findIsResumeReportExists(negId))
            throw new NegResumeReportRequiredException();
        if (negFileService.findIsUnsignedFilesExists(negId))
            throw new AllDocFilesShouldBeSignedException();
    }

    private boolean isNegResumePublished(String negStatus) {
        return Arrays.asList("AWARDED", "NEG_FAILED", "FIRST_STAGE_FINISHED").contains(negStatus);
    }

    protected boolean isNegVotingFinished(String negStatus) {
        return "VOTING_FINISHED".equals(negStatus);
    }
}