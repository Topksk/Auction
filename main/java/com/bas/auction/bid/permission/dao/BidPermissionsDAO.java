package com.bas.auction.bid.permission.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.permission.dto.BidLinePermissions;

import java.util.List;

public interface BidPermissionsDAO {
    List<BidLinePermissions> findBidLinePermissions(Long negId);

    List<Integer> findNegInvalidPermissionBidLines(Long negId);

    void update(User user, List<BidLinePermissions> bidLinePermissions);

    void permitNegBidsInFunctionalCurrency(Long negId, Long requirementId);

    void createNegBidPermissions(Long negId, Boolean defaultPermission);

    void createTender2Stage1NegBidPermissions(Long negId);

    void createTender2Stage2NegBidPermissions(Long negId);

}
