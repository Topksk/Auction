package com.bas.auction.neg.unlock.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.permission.service.BidDumpingPermissionService;
import com.bas.auction.bid.permission.service.BidForeignCurrencyPermissionService;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.bid.service.BidLineService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.service.NegNotificationService;
import com.bas.auction.neg.service.NegReportsService;
import com.bas.auction.neg.setting.service.NegSettingService;
import com.bas.auction.neg.unlock.service.NegUnlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class NegUnlockServiceImpl implements NegUnlockService {
    private final Logger logger = LoggerFactory.getLogger(NegUnlockServiceImpl.class);
    private final TransactionTemplate transactionTemplate;
    private NegotiationDAO negDAO;
    private final NegSettingService negSettingService;
    private final BidLineService bidLineService;
    private final BidForeignCurrencyPermissionService bidForeignCurrencyPermService;
    private final BidDumpingPermissionService bidDumpingPermService;
    private final BidPermissionsService bidPermissionsService;
    private final NegReportsService negReports;
    private final NegNotificationService negNotifService;
    private final UserService userService;
    private final BidDAO bidDAO;

    @Autowired
    public NegUnlockServiceImpl(TransactionTemplate transactionTemplate, NegSettingService negSettingService,
                                NegReportsService negReports, NegNotificationService negNotifService,
                                BidLineService bidLineService, BidForeignCurrencyPermissionService bidForeignCurrencyPermService,
                                BidDumpingPermissionService bidDumpingPermService,
                                BidPermissionsService bidPermissionsService, UserService userService,
                                BidDAO bidDAO) {
        this.transactionTemplate = transactionTemplate;
        this.negSettingService = negSettingService;
        this.negReports = negReports;
        this.negNotifService = negNotifService;
        this.bidLineService = bidLineService;
        this.bidForeignCurrencyPermService = bidForeignCurrencyPermService;
        this.bidDumpingPermService = bidDumpingPermService;
        this.bidPermissionsService = bidPermissionsService;
        this.userService = userService;
        this.bidDAO = bidDAO;
    }

    @Autowired
    public void setNegotiationDAO(NegotiationDAO negDAO) {
        this.negDAO = negDAO;
    }

    @Override
    public List<Long> findUnlockList() {
        return negDAO.findUnlockList();
    }

    @Override
    public void autoUnlock(Long negId) {
        transactionTemplate.execute(status -> autoUnlock(status, negId));
    }

    protected Void autoUnlock(TransactionStatus status, Long negId) {
        try {
            unlock(negId);
        } catch (Exception e) {
            logger.error("Unlock exception: negId={}", negId, e);
            status.setRollbackOnly();
        }
        return null;
    }

    @Override
    public Negotiation unlock(Long negId) {
        logger.debug("unlock neg: negId={}", negId);
        User user = User.sysadmin();
        updateStatusToVoting(user.getUserId(), negId);
        Negotiation neg = negDAO.findAdminNegHeader(negId);
        boolean isTender2Stage1 = neg.isTender2Stage1();
        boolean isTender2Stage2 = neg.isTender2Stage2();

        // delete bid lines with 0 or empty price
        bidLineService.deleteNotParticipatedBidLines(neg);

        bidPermissionsService.createNegBidPermissions(neg);
        if (!isTender2Stage1) {
            if (negSettingService.isNegForeignCurrencyControlEnabled(negId))
                bidForeignCurrencyPermService.performNegBidsForeignCurrencyControl(user, negId);
            else
                bidDAO.updateAllBidsUnlockExchangeRate(negId);
        }
        if (neg.isTender() || isTender2Stage2)
            bidDumpingPermService.performNegBidsDumpingControl(user, neg);

        if (neg.isTender() || neg.isTender2()) {
            neg = generateUnlockReport(user, neg);
        } else {
            neg = negDAO.findAdminNeg(user, negId);
        }
        negDAO.updateIndexAsync(neg);
        return neg;
    }

    protected Negotiation generateUnlockReport(User user, Negotiation neg) {
        logger.debug("generate unlock report: negId={}", neg.getNegId());
        User creator = userService.findById(neg.getCreatedBy());
        negReports.generateUnlockReport(creator, neg.getNegId());
        neg = negDAO.findAdminNeg(user, neg.getNegId());
        negNotifService.sendUnlockRepPublishNotif(neg);
        return neg;
    }

    private void updateStatusToVoting(Long userId, Long negId) {
        negDAO.updateStatus(userId, negId, "VOTING");
    }
}
