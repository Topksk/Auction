package com.bas.auction.neg.setting.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.dto.Negotiation.NegType;
import com.bas.auction.neg.setting.dao.NegDiscountDAO;
import com.bas.auction.neg.setting.dto.NegDiscount;
import com.bas.auction.neg.setting.dto.NegDiscountVal;
import com.bas.auction.neg.setting.service.NegDiscountService;
import com.bas.auction.neg.setting.service.NegDiscountValuesService;
import com.bas.auction.profile.customer.setting.dao.MdDiscountDAO;
import com.bas.auction.profile.customer.setting.dto.MdDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Service
public class NegDiscountServiceImpl implements NegDiscountService {
    private final NegDiscountDAO negDiscountDAO;
    private final MdDiscountDAO mdDiscountDAO;
    private final NegDiscountValuesService negDiscountValuesService;

    @Autowired
    public NegDiscountServiceImpl(NegDiscountDAO negDiscountDAO, MdDiscountDAO mdDiscountDAO, NegDiscountValuesService negDiscountValuesService) {
        this.negDiscountDAO = negDiscountDAO;
        this.mdDiscountDAO = mdDiscountDAO;
        this.negDiscountValuesService = negDiscountValuesService;
    }

    @Override
    public Map<Long, NegDiscount> findNegDiscounts(Long negId) {
        Map<Long, List<NegDiscountVal>> negDiscountValues =
                negDiscountValuesService
                        .findNegDiscountValues(negId).stream()
                        .collect(groupingBy(NegDiscountVal::getDiscountId));
        List<NegDiscount> negDiscounts = negDiscountDAO.findNegDiscounts(negId);
        negDiscounts.forEach(nd -> nd.setValues(negDiscountValues.get(nd.getDiscountId())));
        return negDiscounts.stream()
                .filter(NegDiscount::getDisplayInForm)
                .collect(toMap(NegDiscount::getDiscountId, identity()));
    }

    @Override
    public void create(Negotiation neg) {
        List<MdDiscount> mdDiscounts = findMdDiscounts(neg.getSettingId(), neg.getNegType());
        List<NegDiscount> negDiscounts = mdDiscounts.stream()
                .map(mdDiscount -> mapToNegDiscount(neg, mdDiscount))
                .collect(toList());
        negDiscountDAO.insert(negDiscounts);
        negDiscountValuesService.create(neg, negDiscounts);
    }

    private NegDiscount mapToNegDiscount(Negotiation neg, MdDiscount mdDiscount) {
        NegDiscount negDiscount = new NegDiscount();
        negDiscount.setNegId(neg.getNegId());
        negDiscount.setDiscountId(mdDiscount.getDiscountId());
        negDiscount.setDescription(mdDiscount.getDescription());
        negDiscount.setDiscountCode(mdDiscount.getDiscountCode());
        negDiscount.setDiscountType(mdDiscount.getDiscountType());
        negDiscount.setApplicableForGood(mdDiscount.isApplicableForGood());
        negDiscount.setApplicableForWork(mdDiscount.isApplicableForWork());
        negDiscount.setApplicableForService(mdDiscount.isApplicableForService());
        negDiscount.setApplicableForStage2(mdDiscount.isApplicableForStage2());
        negDiscount.setDisplayInForm(mdDiscount.getDisplayInForm());
        negDiscount.setIsSystem(mdDiscount.getIsSystem());
        negDiscount.setCreatedBy(neg.getCreatedBy());
        negDiscount.setLastUpdatedBy(neg.getLastUpdatedBy());
        return negDiscount;
    }

    @Override
    public void copyNegDiscounts(User user, Long sourceNegId, Long destinationNegId) {
        List<NegDiscount> negDiscounts = negDiscountDAO.findNegDiscounts(sourceNegId);
        if (negDiscounts.isEmpty())
            return;
        negDiscounts.forEach(negDiscount -> {
            negDiscount.setNegId(destinationNegId);
            negDiscount.setCreatedBy(user.getUserId());
            negDiscount.setLastUpdatedBy(user.getUserId());
        });
        negDiscountDAO.insert(negDiscounts);
        negDiscountValuesService.copyNegDiscountValues(user, sourceNegId, destinationNegId);
    }

    private List<MdDiscount> findMdDiscounts(Long settingId, NegType negType) {
        if (negType == NegType.TENDER) {
            return mdDiscountDAO.findTenderDiscounts(settingId);
        } else if (negType == NegType.TENDER2) {
            return mdDiscountDAO.findTender2Discounts(settingId);
        }
        throw new IllegalArgumentException("Illegal negotiation type for discounts");
    }

    @Override
    public void delete(Long negId) {
        negDiscountValuesService.delete(negId);
        negDiscountDAO.delete(negId);
    }
}
