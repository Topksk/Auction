package com.bas.auction.bid.permission.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.permission.dao.BidPermissionsDAO;
import com.bas.auction.bid.permission.dto.BidLinePermissionDetails;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Repository
public class BidPermissionsDAOImpl implements BidPermissionsDAO, GenericDAO<BidLinePermissions> {
    private final Logger logger = LoggerFactory.getLogger(BidPermissionsDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final Conf conf;

    @Autowired
    public BidPermissionsDAOImpl(DaoJdbcUtil daoutil, Conf conf) {
        this.daoutil = daoutil;
        this.conf = conf;
    }

    @Override
    public String getSqlPath() {
        return "bid_perms";
    }

    @Override
    public Class<BidLinePermissions> getEntityType() {
        return BidLinePermissions.class;
    }

    private BidLinePermissionDetails mapToBidLinePermissionDetail(Map<String, Object> entry) {
        BidLinePermissionDetails blp = new BidLinePermissionDetails();
        blp.setRequirementId((Long) entry.get("requirement_id"));
        blp.setPermitted((Boolean) entry.get("permitted"));
        blp.setSystem((Boolean) entry.get("is_system"));
        blp.setRejectReason((String) entry.get("reject_reason"));
        blp.setDescription((String) entry.get("description"));
        return blp;
    }

    private Map<Long, Map<Integer, List<BidLinePermissionDetails>>> findAllBidLinePermissionDetails(long negId) {
        List<Map<String, Object>> bidLinesPermissions = daoutil.queryForMapList(this, "get_all_bid_lines_permissions", negId);
        return bidLinesPermissions.stream()
                .collect(groupingBy(e -> (Long) e.get("bid_id"),
                        groupingBy(e -> (Integer) e.get("bid_line_num"), mapping(this::mapToBidLinePermissionDetail, toList()))));
    }

    @Override
    public List<BidLinePermissions> findBidLinePermissions(Long negId) {
        logger.debug("find bid permissions for neg: {}", negId);
        List<BidLinePermissions> res = new ArrayList<>();
        Map<Long, Map<Integer, List<BidLinePermissionDetails>>> allBidPermissions = findAllBidLinePermissionDetails(negId);
        List<Map<String, Object>> bidSupplierInfo = daoutil.queryForMapList(this, "get_neg_bid_supplier", negId);
        for (Map<String, Object> entry : bidSupplierInfo) {
            Long bidId = (Long) entry.get("bid_id");
            Long supplierId = (Long) entry.get("supplier_id");
            String supplierName = (String) entry.get("name_ru");
            Map<Integer, List<BidLinePermissionDetails>> perm = allBidPermissions.get(bidId);
            if (perm == null)
                continue;
            for (Entry<Integer, List<BidLinePermissionDetails>> e : perm.entrySet()) {
                BidLinePermissions bp = new BidLinePermissions();
                bp.setBidId(bidId);
                bp.setBidLineNum(e.getKey());
                bp.setSupplierId(supplierId);
                bp.setSupplierName(supplierName);
                bp.setPermissions(e.getValue());
                res.add(bp);
            }
        }
        return res;
    }

    @Override
    public List<Integer> findNegInvalidPermissionBidLines(Long negId) {
        return daoutil.queryScalarList(this, "get_neg_invalid_permission_bid_lines", negId);
    }

    @Override
    public void update(User user, List<BidLinePermissions> bidLinePermissions) {
        logger.debug("update bid permissions");
        List<Object[]> values = bidLinePermissions.stream()
                .flatMap(this::updateValues)
                .collect(Collectors.toList());
        daoutil.batchUpdate(this, values);
    }

    private Stream<Object[]> updateValues(BidLinePermissions bidPerms) {
        return bidPerms.getPermissions().stream()
                .map(linePermDetails -> updateValues(bidPerms.getBidId(), bidPerms.getBidLineNum(), linePermDetails));
    }

    private Object[] updateValues(Long bidId, Integer bidLineNum, BidLinePermissionDetails linePermDetails) {
        return new Object[]{linePermDetails.isPermitted(), linePermDetails.getRejectReason(),
                linePermDetails.getLastUpdatedBy(), bidId, bidLineNum, linePermDetails.getRequirementId()};
    }

    @Override
    public void permitNegBidsInFunctionalCurrency(Long negId, Long requirementId) {
        Object[] params = {negId, conf.getFunctionalCurrency(), requirementId};
        daoutil.dml(this, "update_func_currency_bids_perms", params);
    }

    @Override
    public void createNegBidPermissions(Long negId, Boolean defaultPermission) {
        Long userId = User.sysadmin().getUserId();
        Object[] params = {defaultPermission, userId, userId, negId};
        daoutil.insert(this, params);
    }

    @Override
    public void createTender2Stage1NegBidPermissions(Long negId) {
        Long userId = User.sysadmin().getUserId();
        Object[] params = {userId, userId, negId};
        daoutil.dml(this, "insert_tender2_stage1", params);
    }

    @Override
    public void createTender2Stage2NegBidPermissions(Long negId) {
        Long userId = User.sysadmin().getUserId();
        Object[] params = {userId, userId, negId};
        daoutil.dml(this, "insert_tender2_stage2", params);
    }
}
