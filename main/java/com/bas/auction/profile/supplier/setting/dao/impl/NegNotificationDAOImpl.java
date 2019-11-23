package com.bas.auction.profile.supplier.setting.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.profile.supplier.dto.NegNotification;
import com.bas.auction.profile.supplier.service.SupplierPercolateService;
import com.bas.auction.profile.supplier.setting.dao.NegNotificationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Repository
public class NegNotificationDAOImpl implements NegNotificationDAO, GenericDAO<NegNotification> {
    private final DaoJdbcUtil daoutil;
    private final SupplierPercolateService percolateService;

    @Autowired
    public NegNotificationDAOImpl(DaoJdbcUtil daoutil, SupplierPercolateService percolateService) {
        this.daoutil = daoutil;
        this.percolateService = percolateService;
    }

    @Override
    public Class<NegNotification> getEntityType() {
        return NegNotification.class;
    }

    @Override
    public String getSqlPath() {
        return "supplier_settings/neg_notifications";
    }

    @Override
    public List<NegNotification> findNotifications(Long settingId) {
        return daoutil.query(this, "list", settingId);
    }

    @Override
    @SpringTransactional
    public void upsert(final Long settingId, User user, List<NegNotification> notifications) {
        List<Object[]> insertValues = new ArrayList<>();
        List<Object[]> updateValues = new ArrayList<>();
        for (NegNotification notification : notifications) {
            boolean exist = isExist(notification.getNotificationId());
            if (exist) {
                updateValues.add(updateFieldValues(user, notification));
            } else {
                insertValues.add(insertFieldValues(settingId, user, notification));
            }
        }

        removeDeletedNotifications(settingId, user.getUserId(), notifications);
        if (!insertValues.isEmpty()) {
            daoutil.batchInsert(this, insertValues);
        }
        if (!updateValues.isEmpty()) {
            daoutil.batchUpdate(this, updateValues);
        }

        notifications = findNotifications(settingId);
        notifications.forEach(notification -> percolateService.subscribe(user.getUserId(), notification));
    }

    private boolean isExist(final Long notificationId) {
        return daoutil.exists(this, "exist", notificationId);
    }

    private void removeDeletedNotifications(final Long settingId, final Long userId, List<NegNotification> notifications) {
        List<Long> deletedIds = getDeletedNotificationIds(settingId, notifications);
        if (!deletedIds.isEmpty()) {
            List<Object[]> deleteParams = deletedIds.stream()
                    .map(id -> new Object[]{id})
                    .collect(Collectors.toList());
            daoutil.batchDelete(this, deleteParams);
        }
        List<String> percolateIds = deletedIds.stream()
                .map(id -> userId + "_" + id)
                .collect(Collectors.toList());
        percolateService.deleteSubscribe(percolateIds);
    }

    private List<Long> getDeletedNotificationIds(final Long settingId, List<NegNotification> notifications) {

        List<Long> existingNotificationIds = findNotifications(settingId).stream()
                .map(NegNotification::getNotificationId)
                .collect(toList());

        List<Long> givenNotificationIds = notifications.stream()
                .map(NegNotification::getNotificationId)
                .collect(toList());

        existingNotificationIds.removeAll(givenNotificationIds);

        return existingNotificationIds;
    }

    private Object[] insertFieldValues(final long settingId, User user, NegNotification notification) {
        return new Object[]{settingId, notification.getCategory(), notification.getAmount(),
                user.getUserId(), user.getUserId()};
    }

    private Object[] updateFieldValues(User user, NegNotification notification) {
        return new Object[]{notification.getCategory(), notification.getAmount(), user.getUserId(), notification.getNotificationId()};
    }
}
