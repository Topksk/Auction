package com.bas.auction.neg.auction.service.impl;

import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.neg.auction.service.AuctionExtendService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuctionExtendServiceImpl implements AuctionExtendService {
    private final CustomerSettingDAO customerSettingDAO;
    private final NegotiationDAO negDAO;

    @Autowired
    public AuctionExtendServiceImpl(CustomerSettingDAO customerSettingDAO, NegotiationDAO negDAO) {
        this.customerSettingDAO = customerSettingDAO;
        this.negDAO = negDAO;
    }

    @Override
    @SpringTransactional
    public void extendAuction(Long negId) {
        Long settingId = negDAO.findSettingId(negId);
        CustomerSetting setting = customerSettingDAO.findAuctionSettings(settingId);
        if (setting.getAuctionExtTimeLeft() == null || setting.getAuctionExtNumber() == null)
            return;
        double secondsLeftToClose = negDAO.findSecondsLeftToClose(negId);
        double minutesLeftToClose = secondsLeftToClose / 60.00;
        if (minutesLeftToClose < setting.getAuctionExtTimeLeft()) {
            Integer auctionExtendCount = negDAO.findExtendCount(negId);
            if (auctionExtendCount < setting.getAuctionExtNumber()) {
                negDAO.updateForAuctionExtend(negId, setting.getAuctionExtDuration());
            }
        }
    }
}
