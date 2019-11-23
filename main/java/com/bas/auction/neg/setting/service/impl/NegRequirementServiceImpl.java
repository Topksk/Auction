package com.bas.auction.neg.setting.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.setting.dao.NegRequirementDAO;
import com.bas.auction.neg.setting.dto.NegRequirement;
import com.bas.auction.neg.setting.service.NegRequirementService;
import com.bas.auction.profile.customer.setting.dao.MdRequirementDAO;
import com.bas.auction.profile.customer.setting.dto.MdRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegRequirementServiceImpl implements NegRequirementService {
    private final NegRequirementDAO negRequirementDAO;
    private final MdRequirementDAO mdRequirementDAO;

    @Autowired
    public NegRequirementServiceImpl(NegRequirementDAO negRequirementDAO, MdRequirementDAO mdRequirementDAO) {
        this.negRequirementDAO = negRequirementDAO;
        this.mdRequirementDAO = mdRequirementDAO;
    }

    @Override
    public void create(Negotiation neg) {
        List<MdRequirement> mdRequirements = findMdRequirements(neg.getSettingId(), neg.getNegType());
        List<NegRequirement> negRequirements = mdRequirements.stream()
                .map(mdRequirement -> mapToNegRequirement(neg, mdRequirement))
                .collect(Collectors.toList());
        negRequirementDAO.insert(negRequirements);
    }

    private NegRequirement mapToNegRequirement(Negotiation neg, MdRequirement mdRequirement) {
        NegRequirement negRequirement = new NegRequirement();
        negRequirement.setNegId(neg.getNegId());
        negRequirement.setRequirementId(mdRequirement.getRequirementId());
        negRequirement.setReqType(mdRequirement.getReqType());
        negRequirement.setDescription(mdRequirement.getDescription());
        negRequirement.setApplicableForStage1(mdRequirement.isApplicableForStage1());
        negRequirement.setApplicableForStage2(mdRequirement.isApplicableForStage2());
        negRequirement.setCreatedBy(neg.getCreatedBy());
        negRequirement.setLastUpdatedBy(neg.getLastUpdatedBy());
        return negRequirement;
    }

    @Override
    public void copyNegRequirements(User user, Long sourceNegId, Long destinationNegId) {
        List<NegRequirement> negRequirements = negRequirementDAO.findNegRequirements(sourceNegId);
        negRequirements.forEach(negRequirement -> {
            negRequirement.setNegId(destinationNegId);
            negRequirement.setCreatedBy(user.getUserId());
            negRequirement.setLastUpdatedBy(user.getUserId());
        });
        negRequirementDAO.insert(negRequirements);
    }


    private List<MdRequirement> findMdRequirements(Long settingId, NegType negType) {
        switch (negType) {
            case AUCTION:
                return mdRequirementDAO.findAuctionReqs(settingId);
            case RFQ:
                return mdRequirementDAO.findRfqReqs(settingId);
            case TENDER:
                return mdRequirementDAO.findTenderReqs(settingId);
            case TENDER2:
                return mdRequirementDAO.findTender2Reqs(settingId);
            default:
                throw new IllegalArgumentException("Illegal negotiation type for requirements");
        }
    }

    @Override
    public void delete(Long negId) {
        negRequirementDAO.delete(negId);
    }

    @Override
    public Long findForeignCurrencyControlRequirementId(Long negId) {
        return negRequirementDAO.findForeignCurrencyControlRequirementId(negId);
    }

    @Override
    public Long findDumpingControlRequirementId(Long negId) {
        return negRequirementDAO.findDumpingControlRequirementId(negId);
    }
}
