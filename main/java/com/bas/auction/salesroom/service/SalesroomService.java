package com.bas.auction.salesroom.service;

import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

public interface SalesroomService {
    @EventListener
    void onDisconnect(SessionDisconnectEvent event);

    @EventListener
    void onUnsubscribe(SessionUnsubscribeEvent event);

    @EventListener
    void onSubscribe(SessionSubscribeEvent event);

    void sendNotification(Long negId);
}
