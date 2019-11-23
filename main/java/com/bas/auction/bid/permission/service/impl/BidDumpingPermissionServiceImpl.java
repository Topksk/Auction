package com.bas.auction.bid.permission.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.bid.permission.service.BidDumpingPermissionService;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.service.NegRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Service
public class BidDumpingPermissionServiceImpl implements BidDumpingPermissionService {
    private final MessageDAO messageDAO;
    private final BidPermissionsService bidPermsService;
    private final BidDAO bidDAO;
    private final NegRequirementService negRequirementService;

    @Autowired
    public BidDumpingPermissionServiceImpl(MessageDAO messageDAO, BidDAO bidDAO,
                                           BidPermissionsService bidPermsService,
                                           NegRequirementService negRequirementService) {
        this.messageDAO = messageDAO;
        this.bidDAO = bidDAO;
        this.bidPermsService = bidPermsService;
        this.negRequirementService = negRequirementService;
    }

    @Override
    public void performNegBidsDumpingControl(User user, Negotiation neg) {
        Long requirementId = negRequirementService.findDumpingControlRequirementId(neg.getNegId());
        String rejectReason = messageDAO.getFromDb("DUMPING_CONTROL_REJECT_REASON", "RU");
        BidDumpingPermissionControl bidDumping = buildBidDumpingController(neg, requirementId, rejectReason);
        List<Map<String, Object>> negAndBidLinesPrices = bidDAO.findNegLinesUnitAndBidLinesMeanPrices(neg.getNegId());
        List<BidLinePermissions> allPermissions = negAndBidLinesPrices.stream()
                .map(bidDumping::performBidLineDumpingControl)
                .collect(toList());
        bidPermsService.update(user, allPermissions);
    }

    protected BidDumpingPermissionControl buildBidDumpingController(Negotiation neg, Long requirementId,
                                                                    String rejectReason) {
        return new BidDumpingPermissionControl(neg, requirementId, rejectReason);
    }
}
