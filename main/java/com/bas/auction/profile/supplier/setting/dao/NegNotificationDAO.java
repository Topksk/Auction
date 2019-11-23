package com.bas.auction.profile.supplier.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.supplier.dto.NegNotification;

import java.util.List;

/**
 * Created by bayangali.nauryz on 18.12.2015.
 */
public interface NegNotificationDAO {
    List<NegNotification> findNotifications(Long settingId);

    void upsert(final Long settingId, User user, List<NegNotification> notifications);
}
