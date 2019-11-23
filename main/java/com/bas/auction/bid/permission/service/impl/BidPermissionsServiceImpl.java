package com.bas.auction.bid.permission.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.permission.dao.BidPermissionsDAO;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.bid.permission.service.BidPermissionValidationService;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.dto.Negotiation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BidPermissionsServiceImpl implements BidPermissionsService {
    private final Logger logger = LoggerFactory.getLogger(BidPermissionsServiceImpl.class);
    private final BidPermissionsDAO bidPermsDAO;
    private final BidPermissionValidationService bidPermissionValidationService;

    @Autowired
    public BidPermissionsServiceImpl(BidPermissionsDAO bidPermsDAO, BidPermissionValidationService bidPermissionValidationService) {
        this.bidPermsDAO = bidPermsDAO;
        this.bidPermissionValidationService = bidPermissionValidationService;
    }

    @Override
    @SpringTransactional
    public void update(User user, List<BidLinePermissions> bidLinePermissions) {
        if (bidLinePermissions.isEmpty())
            return;
        long bidId = bidLinePermissions.get(0).getBidId();
        bidPermissionValidationService.validateUpdate(bidId);
        setBidLinePermissionsLastUpdatedUserId(user, bidLinePermissions);
        bidPermsDAO.update(user, bidLinePermissions);
    }

    private void setBidLinePermissionsLastUpdatedUserId(User user, List<BidLinePermissions> bidLinePermissions) {
        Long userId = user.getUserId();
        bidLinePermissions.stream().flatMap(bidPerms -> bidPerms.getPermissions().stream())
                .forEach(bidLinePerm -> bidLinePerm.setLastUpdatedBy(userId));
    }

    @Override
    @SpringTransactional
    public void permitNegBidsInFunctionalCurrency(Long negId, Long requirementId) {
        bidPermsDAO.permitNegBidsInFunctionalCurrency(negId, requirementId);
    }

    @Override
    @SpringTransactional
    public void createNegBidPermissions(Negotiation neg, Boolean defaultPermission) {
        logger.debug("create neg bid permissions: negId={}, negType={}", neg.getNegId(), neg.getNegType());
        long negId = neg.getNegId();
        if (neg.isTender2Stage1()) {
            logger.debug("create tender2 stage1 bid permissions");
            bidPermsDAO.createTender2Stage1NegBidPermissions(negId);
        } else if (neg.isTender2Stage2()) {
            logger.debug("create tender2 stage2 bid permissions");
            bidPermsDAO.createTender2Stage2NegBidPermissions(negId);
        } else {
            bidPermsDAO.createNegBidPermissions(negId, defaultPermission);
        }
    }

    @Override
    public void createNegBidPermissions(Negotiation neg) {
        createNegBidPermissions(neg, null);
    }
}
