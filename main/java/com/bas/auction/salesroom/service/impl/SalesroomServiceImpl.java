package com.bas.auction.salesroom.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.salesroom.service.SalesroomService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class SalesroomServiceImpl implements SalesroomService {
    private final static Logger logger = LoggerFactory.getLogger(SalesroomServiceImpl.class);
    private final ConcurrentMap<User, Map<Long, String>> users = new ConcurrentHashMap<>();
    private final String destinationPrefix = "/user/queue/negs.";
    private final int destinationPrefixLength = destinationPrefix.length();
    private final BidDAO bidDAO;
    private final Utils utils;
    private final ExchangeRateService exchangeRateService;
    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public SalesroomServiceImpl(BidDAO bidDAO, Utils utils, ExchangeRateService exchangeRateService, SimpMessageSendingOperations messagingTemplate) {
        this.bidDAO = bidDAO;
        this.utils = utils;
        this.exchangeRateService = exchangeRateService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    @Override
    public void onDisconnect(SessionDisconnectEvent event) {
        removeUser(event);
    }

    @EventListener
    @Override
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        removeUser(event);
    }

    @EventListener
    @Override
    public void onSubscribe(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        Authentication authentication = (Authentication) SimpMessageHeaderAccessor.getUser(headers);
        if (authentication == null) {
            return;
        }
        String destination = SimpMessageHeaderAccessor.getDestination(headers);
        if (!destination.startsWith(destinationPrefix))
            return;
        String param = destination.substring(destinationPrefixLength).trim();
        if (!param.matches("^\\w{3}\\.\\d+$")) {
            logger.debug("incorrect neg subscribtion: {}", param);
            return;
        }
        String currency = param.substring(0, 3);
        String negId = param.substring(4);
        User user = (User) authentication.getPrincipal();
        Map<Long, String> negIds = users.get(user);
        if (negIds == null) {
            negIds = new ConcurrentHashMap<>();
            users.put(user, negIds);
        }
        negIds.put(Long.valueOf(negId), currency);
        logger.debug("salesroom subscribe: user={}, negId:={}, currency={}", user.getUsername(), negId, currency);
    }

    @Override
    public void sendNotification(Long negId) {
        List<User> subscribers = users.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(negId))
                .map(Entry::getKey)
                .collect(toList());
        if (subscribers.isEmpty())
            return;
        logger.debug("send salesroom notif: negId={}", negId);
        Map<Integer, BigDecimal> bestPrices = bidDAO.findSalesroomBestPrices(negId);
        Map<Long, Map<Integer, Long>> ranks = bidDAO.findAllSuppliersRanks(negId);
        Gson gson = utils.getGsonForClient();
        String destination = "/queue/negs.";
        Map<String, BigDecimal> exchangeRates = new HashMap<>();
        for (User user : subscribers) {
            String currency = users.get(user).get(negId);
            BigDecimal rate = exchangeRates.computeIfAbsent(currency, exchangeRateService::findCurrentExchangeRate);
            Map<String, Object> data = new HashMap<>();
            data.put("recid", negId);
            data.put("best_prices", convertBestPrices(rate, bestPrices));
            data.put("ranks", ranks.get(user.getSupplierId()));
            String finalDestination = "/user/" + user.getUsername() + destination + currency + "." + negId;
            logger.debug("send salesroom notif: user={}, currency={}", user.getUsername(), currency);
            messagingTemplate.send(finalDestination, toJsonPayload(gson, data));
        }
    }

    private Map<Integer, BigDecimal> convertBestPrices(BigDecimal rate, Map<Integer, BigDecimal> bestPrices) {
        return bestPrices.entrySet().stream()
                .collect(toMap(Entry::getKey,
                        e -> e.getValue().divide(rate, 2, RoundingMode.DOWN)));
    }

    private Message<byte[]> toJsonPayload(Gson gson, Object data) {
        String json = gson.toJson(data);
        json = StringEscapeUtils.escapeJson(json);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        MessageBuilder<byte[]> messageBuilder = MessageBuilder.withPayload(bytes);
        return messageBuilder.build();
    }

    private void removeUser(AbstractSubProtocolEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        Authentication authentication = (Authentication) SimpMessageHeaderAccessor.getUser(headers);
        if (authentication == null) {
            return;
        }
        User user = (User) authentication.getPrincipal();
        users.remove(user);
        logger.debug("salesroom unsubscribe: user={}", user.getUsername());
    }

}