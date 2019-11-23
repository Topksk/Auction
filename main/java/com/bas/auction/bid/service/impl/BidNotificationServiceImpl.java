package com.bas.auction.bid.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.service.BidNotificationService;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.profile.employee.dao.PersonDAO;
import com.bas.auction.profile.employee.dto.Person;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.salesroom.service.SalesroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BidNotificationServiceImpl implements BidNotificationService {
    private final Conf conf;
    private final MessageDAO messages;
    private final EmployeeService employeeService;
    private final MailService mailService;
    private final SalesroomService salesroomService;
    private final PersonDAO personDAO;

    @Autowired
    public BidNotificationServiceImpl(Conf conf, MessageDAO messages, EmployeeService employeeService, MailService mailService, SalesroomService salesroomService, PersonDAO personDAO) {
        this.conf = conf;
        this.messages = messages;
        this.employeeService = employeeService;
        this.mailService = mailService;
        this.salesroomService = salesroomService;
        this.personDAO = personDAO;
    }

    @Override
    public void sendAuctionPriceChangeNotif(User user, Bid bid) throws MessagingException {
        salesroomService.sendNotification(bid.getNegId());
        List<Map<String, Object>> list = employeeService.findAllActiveBidAuthorsInfoForNotif(user.getUserId(),
                bid.getNegId());
        String subject = messages.getFromDb("AUCTION_PRICE_CHANGED_SUBJECT", "RU");
        Map<String, String> params = new HashMap<>();
        params.put("AUCTION_TITLE", bid.getNeg().getTitle());
        params.put("NEG_ID", Long.toString(bid.getNeg().getNegId()));
        String actUrl = conf.getHost() + "/supplier.html#saleroom/" + bid.getNeg().getNegId();
        params.put("SALEROOM_LINK", actUrl);
        for (Map<String, Object> row : list) {
            params.put("NAME", row.get("full_name").toString());
            String content = messages.getFromDb("AUCTION_PRICE_CHANGED_BODY", "RU", params);
            mailService.sendMail(row.get("email").toString(), subject, content);
        }
    }

    @Override
    public void sendBidSentNotification(User user, Bid bid) throws MessagingException {
        Person person = personDAO.findById(user.getPersonId());

        StringBuilder fullName = new StringBuilder();
        if (person.getLastName() != null)
            fullName.append(person.getLastName());
        if (person.getFirstName() != null)
            fullName.append(" ").append(person.getFirstName());
        if (person.getMiddleName() != null)
            fullName.append(" ").append(person.getMiddleName());

        Map<String, String> params = new HashMap<>();
        params.put("NEG_ID", bid.getNeg().getDocNumber());
        params.put("BID_ID", Long.toString(bid.getBidId()));
        params.put("FIO", fullName.toString());

        String subject = messages.getFromDb("SUPPLIER_BID_SENT_NOTIFICATION_SUBJECT", "RU", params);
        String content = messages.getFromDb("SUPPLIER_BID_SENT_NOTIFICATION_BODY", "RU", params);
        mailService.sendMail(user.getEmail(), subject, content);
    }
}
