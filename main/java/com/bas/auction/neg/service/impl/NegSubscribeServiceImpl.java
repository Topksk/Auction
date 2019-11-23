package com.bas.auction.neg.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.neg.dto.NegLine;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.service.NegSubscribeService;
import com.bas.auction.profile.customer.dao.CustomerDAO;
import com.bas.auction.profile.supplier.service.SupplierService;
import com.bas.auction.search.CriteriaType;
import com.bas.auction.search.SearchService;
import com.bas.auction.search.SimpleCriteriaType;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NegSubscribeServiceImpl implements NegSubscribeService {
    private final Logger logger = LoggerFactory.getLogger(NegSubscribeServiceImpl.class);
    private final SearchService searchService;
    private final UserService userService;
    private final MailService mailService;
    private final MessageDAO messages;
    private final CustomerDAO customers;
    private final SupplierService supplierService;
    private final Conf conf;

    @Autowired
    public NegSubscribeServiceImpl(SearchService searchService, UserService userService, MailService mailService,
                                   MessageDAO messages, CustomerDAO customers, SupplierService supplierService, Conf conf) {
        this.searchService = searchService;
        this.userService = userService;
        this.mailService = mailService;
        this.messages = messages;
        this.customers = customers;
        this.supplierService = supplierService;
        this.conf = conf;
    }

    @Override
    public void subscriberNotification(Negotiation neg) {
        Map<Long, Negotiation> subscriberCompany = subscriberCompanyAndNeg(neg);
        if (subscriberCompany.keySet() != null)
            subscriberCompany.keySet().forEach(subscriberId -> {
                List<User> users = userService.findBySupplierId(subscriberId);
                sentNotification(users, subscriberCompany.get(subscriberId));
            });
    }

    @Override
    public void subscriberNotification(List<Negotiation> negs) {
        Map<Long, List<Negotiation>> subscriberCompany = subscriberCompanyAndNegs(negs);
        if (subscriberCompany.keySet() != null)
            subscriberCompany.keySet().forEach(subscriberId -> {
                List<User> users = userService.findBySupplierId(subscriberId);
                sentNotification(users, subscriberCompany.get(subscriberId));
            });
    }

    @Override
    public void notSubscriberNotification(List<Negotiation> negs) {
        List<Long> supplisersId = supplierService.findEmptyNotificationSetting();
        List<User> users = null;
        for (Long id : supplisersId) {
            users = userService.findBySupplierId(id);
            sentNotification(users, negs);
        }
    }

    private void sentNotification(List<User> users, List<Negotiation> negs) {
        String[] recipients = users.stream().filter(user -> (user.isActive() && user.isAccountNonExpired() && user.isAccountNonLocked()))
                .map(User::getEmail)
                .collect(Collectors.toList()).toArray(new String[0]);
        if (recipients != null && recipients.length != 0) {
            try {
                String subject = mailSubject();
                String content = mailContent(negs);
                mailService.sendMail(recipients, subject, content);
            } catch (MessagingException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void sentNotification(List<User> users, Negotiation neg) {
        String[] recipients = users.stream().filter(user -> (user.isActive() && user.isAccountNonExpired() && user.isAccountNonLocked()))
                .map(User::getEmail)
                .collect(Collectors.toList()).toArray(new String[0]);
        if (recipients != null && recipients.length != 0) {
            try {
                String subject = mailSubject(neg);
                String content = mailContent(neg);
                mailService.sendMail(recipients, subject, content);
            } catch (MessagingException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private String mailSubject(Negotiation neg) {
        String bin = customers.findBin(neg.getCustomerId());
        Map<String, String> subjectParams = new HashMap<>();
        subjectParams.put("NEG_ID", String.valueOf(neg.getNegId()));
        subjectParams.put("ORG_BIN", bin);
        subjectParams.put("ORG_NAME", customers.findUserOrg(userService.findById(neg.getCreatedBy())).getNameRu());
        subjectParams.put("NEG_TITLE", neg.getTitle());
        return messages.getFromDb("SUPPLIER_NEG_NOTIFICATION_SUBJECT", "RU", subjectParams);
    }

    private String mailSubject() {
        Map<String, String> subjectParams = new HashMap<>();
        return messages.getFromDb("SUPPLIER_NEG_NOTIFICATION_SUBJECT", "RU", subjectParams);
    }

    private String mailContent(Negotiation neg) {
        StringBuilder lineTable = new StringBuilder("<table border=1 style='border: 1px solid black; border-collapse: collapse;'>");
        String negType = negTypeNameRu(neg.getNegType());

        lineTable.append(messages.get("SUPPLIER_NOTIFICATION_LINE_HEAD"));
        neg.getNegLines().forEach(line -> lineTable.append(mapToHtml(line, negType, neg.getNegId())));
        lineTable.append("</table>");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Map<String, String> objectParams = new HashMap<>();
        objectParams.put("NEG_ID", String.valueOf(neg.getNegId()));
        objectParams.put("NEG_URL", getSupplierUrlWithParam("#negotiation"));
        objectParams.put("NEG_SEARCH_URL", getSupplierUrlWithParam("#negSearch"));
        objectParams.put("NEG_PROFILE_URL", getSupplierUrlWithParam("#profile?setting"));
        objectParams.put("NEG_TYPE", negType);
        objectParams.put("START_DATE", dateFormat.format(neg.getOpenDate()));
        objectParams.put("END_DATE", dateFormat.format(neg.getCloseDate()));
        objectParams.put("TABLE", lineTable.toString());
        return messages.getFromDb("SUPPLIER_NEG_NOTIFICATION_BODY", "RU", objectParams);
    }

    private String mailContent(List<Negotiation> negs) {
        StringBuilder lineTable = new StringBuilder("<table border=1 style='border: 1px solid black; border-collapse: collapse;'>");
        lineTable.append(messages.get("SUPPLIER_NOTIFICATION_LINE_HEAD"));
        negs.forEach(neg -> {
            String negType = negTypeNameRu(neg.getNegType());
            neg.getNegLines().forEach(line -> lineTable.append(mapToHtml(line, negType, neg.getNegId())));
        });
        lineTable.append("</table>");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Map<String, String> objectParams = new HashMap<>();
        objectParams.put("TABLE", lineTable.toString());
        return messages.getFromDb("SUPPLIER_NEG_NOTIFICATION_BODY", "RU", objectParams);
    }

    private String mapToHtml(Negotiation neg) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Map<String, String> params = new HashMap<>();
        params.put("NEG_ID", String.valueOf(neg.getNegId()));
        params.put("START_DATE", dateFormat.format(neg.getOpenDate()));
        params.put("END_DATE", dateFormat.format(neg.getCloseDate()));
        return messages.get("SUPPLIER_NOTIFICATION_NEG_LINE", params);
    }

    private String mapToHtml(NegLine line, String negType, long negId) {
        String lineLink =
                String.format("<a href=\"" + getExternalSiteUrl() + "/torgi/lot/%1$d?neg=%2$d\" target=\"_blank\">%3$s</a>",
                        line.getLineNum(), negId, line.getItemNameRu());

        Map<String, String> params = new HashMap<>();
        params.put("LINE_NUM", String.valueOf(line.getLineNum()));
        params.put("ITEM_NAME_RU", lineLink);
        params.put("NEG_TYPE", negType);
        params.put("QUANTITY", line.getQuantity().toString());
        params.put("MEASURE_UNIT", line.getUomDesc());
        params.put("AMOUNT_WITHOUT_VAT", line.getAmountWithoutVat().toString());
        params.put("SHIPPING_LOCATION", line.getShippingLocation());
        return messages.get("SUPPLIER_NOTIFICATION_LINE", params);
    }

    private String negTypeNameRu(Negotiation.NegType negType) {
        switch (negType) {
            case RFQ:
                return messages.get("RFQ");
            case AUCTION:
                return messages.get("AUCTION");
            case TENDER:
                return messages.get("TENDER");
            case TENDER2:
                return messages.get("TENDER2");
        }
        return "";
    }

    private Map<Long, List<Negotiation>> subscriberCompanyAndNegs(List<Negotiation> negs) {
        Map<Long, List<Negotiation>> subscriberCompany = new HashMap<>();
        negs.forEach(neg -> subscribersAndNegs(neg, subscriberCompany));

        return subscriberCompany;
    }

    private Map<Long, Negotiation> subscriberCompanyAndNeg(Negotiation neg) {
        Map<Long, Negotiation> subscriberCompany = new HashMap<>();
        subscribersAndNeg(neg, subscriberCompany);

        return subscriberCompany;
    }

    private void subscribersAndNeg(Negotiation neg, Map<Long, Negotiation> subscriberCompany) {
        Map<Long, List<BigDecimal>> subscriberMap = new HashMap<>();

        PercolateResponse response = searchService.percolate("negs", neg);
        for (PercolateResponse.Match match : response.getMatches()) {
            String percolatorId = match.getId().string();
            logger.info("percolator:{}", percolatorId);
            User user = userService.findById(Long.valueOf(percolatorId.split("_")[0]));
            if (user == null)
                continue;
            BigDecimal amount = interestedAmount(percolatorId);
            if (amount != null) {
                if (subscriberMap.get(user.getSupplierId()) == null)
                    subscriberMap.put(user.getSupplierId(), new ArrayList<>());
                subscriberMap.get(user.getSupplierId()).add(amount);
            }
        }

        subscriberMap.forEach((supplierId, amounts) -> {
            BigDecimal interestedAmount = amounts.stream().min(BigDecimal::compareTo).get();
            List<NegLine> lines = neg.getNegLines().stream()
                    .filter(line -> line.getAmountWithoutVat().compareTo(interestedAmount) > -1)
                    .collect(Collectors.toList());
            if (lines != null && !lines.isEmpty()) {
                neg.setNegLines(lines);
                subscriberCompany.put(supplierId, neg);
            }
        });
    }

    private void subscribersAndNegs(Negotiation neg, Map<Long, List<Negotiation>> subscriberCompany) {
        Map<Long, List<BigDecimal>> subscriberMap = new HashMap<>();

        PercolateResponse response = searchService.percolate("negs", neg);
        for (PercolateResponse.Match match : response.getMatches()) {
            String percolatorId = match.getId().string();
            logger.info("percolator:{}", percolatorId);
            User user = userService.findById(Long.valueOf(percolatorId.split("_")[0]));
            if (user == null)
                continue;
            BigDecimal amount = interestedAmount(percolatorId);
            if (amount != null) {
                if (subscriberMap.get(user.getSupplierId()) == null)
                    subscriberMap.put(user.getSupplierId(), new ArrayList<>());
                subscriberMap.get(user.getSupplierId()).add(amount);
            }
        }

        subscriberMap.forEach((supplierId, amounts) -> {
            BigDecimal interestedAmount = amounts.stream().min(BigDecimal::compareTo).get();
            List<NegLine> lines = neg.getNegLines().stream()
                    .filter(line -> line.getAmountWithoutVat().compareTo(interestedAmount) > -1)
                    .collect(Collectors.toList());
            if (lines != null && !lines.isEmpty()) {
                neg.setNegLines(lines);
                List<Negotiation> negs = subscriberCompany.get(supplierId);
                if (negs == null) {
                    negs = new ArrayList<Negotiation>();
                }
                negs.add(neg);
                subscriberCompany.put(supplierId, negs);
            }
        });
    }

    private BigDecimal interestedAmount(String percolatorId) {
        Map<String, CriteriaType> qf = new HashMap<>();
        qf.put("_id", SimpleCriteriaType.FILTER);
        Map<String, Object> query = new HashMap<>();
        query.put("_id", percolatorId);
        SearchResponse searchResponse = searchService.search(".percolator", qf, query);
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits != null && hits.length > 0) {
            SearchHit hit = hits[0];
            String amountString = (String) ((Map) ((Map) ((Map) ((Map) ((Map) ((Map) ((Map) hit.getSource()
                    .get("query")).get("filtered")).get("filter")).get("nested")).get("query")).get("range"))
                    .get("neg_lines.amount_without_vat")).get("from");
            return new BigDecimal(amountString);
        }
        return null;
    }

    private String getSupplierUrlWithParam(String pathParameters) {
        return conf.getHost() + "/supplier.html" + pathParameters;
    }

    private String getExternalSiteUrl() {
        String url = conf.getExternalSiteRoot();
        if (url.startsWith("https"))
            url = url.replaceAll("^https", "http");
        return url;
    }

}
