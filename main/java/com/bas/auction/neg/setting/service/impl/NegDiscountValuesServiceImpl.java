package com.bas.auction.neg.setting.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.dao.NegDiscountValueDAO;
import com.bas.auction.neg.setting.dto.NegDiscount;
import com.bas.auction.neg.setting.dto.NegDiscountVal;
import com.bas.auction.neg.setting.service.NegDiscountValuesService;
import com.bas.auction.profile.customer.setting.dao.MdDiscountValDAO;
import com.bas.auction.profile.customer.setting.dto.MdDiscountVal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NegDiscountValuesServiceImpl implements NegDiscountValuesService {
    private final NegDiscountValueDAO negDiscountValueDAO;
    private final MdDiscountValDAO mdDiscountValDAO;

    @Autowired
    public NegDiscountValuesServiceImpl(NegDiscountValueDAO negDiscountValueDAO, MdDiscountValDAO mdDiscountValDAO) {
        this.negDiscountValueDAO = negDiscountValueDAO;
        this.mdDiscountValDAO = mdDiscountValDAO;
    }

    @Override
    public List<NegDiscountVal> findNegDiscountValues(Long negId) {
        return negDiscountValueDAO.findNegDiscountValues(negId);
    }

    @Override
    public void create(Negotiation neg, List<NegDiscount> negDiscounts) {
        List<NegDiscountVal> negDiscountVals = negDiscounts.stream()
                .map(this::findMdDiscountVals)
                .flatMap(List::stream)
                .map(mdDiscountVal -> mapToNegDiscountVal(neg, mdDiscountVal))
                .collect(Collectors.toList());
        negDiscountValueDAO.insert(negDiscountVals);
    }

    private NegDiscountVal mapToNegDiscountVal(Negotiation neg, MdDiscountVal mdDiscountVal) {
        NegDiscountVal negDiscountVal = new NegDiscountVal();
        negDiscountVal.setNegId(neg.getNegId());
        negDiscountVal.setDiscountId(mdDiscountVal.getDiscountId());
        negDiscountVal.setDiscount(mdDiscountVal.getDiscount());
        negDiscountVal.setDiscountValId(mdDiscountVal.getDiscountValId());
        negDiscountVal.setBoolValue(mdDiscountVal.getBoolValue());
        negDiscountVal.setNumberFrom(mdDiscountVal.getNumberFrom());
        negDiscountVal.setNumberTo(mdDiscountVal.getNumberTo());
        negDiscountVal.setCreatedBy(neg.getCreatedBy());
        negDiscountVal.setLastUpdatedBy(neg.getLastUpdatedBy());
        return negDiscountVal;
    }

    @Override
    public void copyNegDiscountValues(User user, Long sourceNegId, Long destinationNegId) {
        List<NegDiscountVal> negDiscountValues = negDiscountValueDAO.findNegDiscountValues(sourceNegId);
        negDiscountValues.forEach(negDiscountVal -> {
            negDiscountVal.setNegId(destinationNegId);
            negDiscountVal.setCreatedBy(user.getUserId());
            negDiscountVal.setLastUpdatedBy(user.getUserId());
        });
        negDiscountValueDAO.insert(negDiscountValues);
    }

    private List<MdDiscountVal> findMdDiscountVals(NegDiscount negDiscount) {
        return mdDiscountValDAO.findNegDiscountValues(negDiscount.getDiscountId());
    }

    @Override
    public void delete(Long negId) {
        negDiscountValueDAO.delete(negId);
    }
}
