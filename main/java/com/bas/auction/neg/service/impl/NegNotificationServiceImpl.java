package com.bas.auction.neg.service.impl;

import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.neg.dto.NegTeam;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.service.NegNotificationService;
import com.bas.auction.neg.service.NegotiationNotificationSendingException;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;

@Service
public class NegNotificationServiceImpl implements NegNotificationService {
    private final Logger logger = LoggerFactory.getLogger(NegNotificationServiceImpl.class);
    private final EmployeeService employeeService;
    private final MessageDAO messages;
    private final Conf conf;
    private final MailService mailService;

    @Autowired
    public NegNotificationServiceImpl(Conf conf, EmployeeService employeeService, MessageDAO messages, MailService mailService) {
        this.conf = conf;
        this.employeeService = employeeService;
        this.messages = messages;
        this.mailService = mailService;
    }

    @Override
    public List<Optional<Future<Void>>> sendUnlockRepPublishNotif(Negotiation neg) {
        logger.debug("sending neg unlock notifications: {}", neg.getNegId());
        String subjectCode = "UNLOCK_REPORT_PUBLISHED_SUBJECT";
        String contentCode = "UNLOCK_REPORT_PUBLISHED_BODY";
        sendEmailToNegTeamMembers(neg, subjectCode, contentCode);
        List<Employee> supplierEmployees = employeeService.findNegBidsSuppEmployees(neg.getNegId());
        return sendEmailToSupplierEmployees(neg, supplierEmployees, subjectCode, contentCode);
    }

    @Override
    public List<Optional<Future<Void>>> sendTender2Stage2PublishNotif(Negotiation neg) {
        logger.debug("sending tender2 stage2 publish notif: {}", neg.getNegId());
        String subjectCode = "TENDER2_STAGE2_PUBLISHED_SUBJECT";
        String contentCode = "TENDER2_STAGE2_PUBLISHED_BODY";
        List<Employee> supplierEmployees = employeeService.findTender2Stage1BidsSuppEmployees(neg.getParentNegId());
        return sendEmailToSupplierEmployees(neg, supplierEmployees, subjectCode, contentCode);
    }

    @Override
    public List<Optional<Future<Void>>> sendResumeRepNotif(Negotiation neg) {
        logger.debug("sending neg resume report notifications: {}", neg.getNegId());
        String subjectCode = "RESUME_REPORT_READY_SUBJECT";
        String contentCode = "RESUME_REPORT_READY_BODY";
        return sendEmailToNegTeamMembers(neg, subjectCode, contentCode);
    }

    @Override
    public List<Optional<Future<Void>>> sendResumeRepPublishNotif(Negotiation neg) {
        logger.debug("send resume rep publish notif: {}", neg.getNegId());
        String subjectCode = "RESUME_REPORT_PUBLISHED_SUBJECT";
        String contentCode = "RESUME_REPORT_PUBLISHED_BODY";
        sendEmailToNegTeamMembers(neg, subjectCode, contentCode);
        List<Employee> supplierEmployees = employeeService.findNegBidsSuppEmployees(neg.getNegId());
        return sendEmailToSupplierEmployees(neg, supplierEmployees, subjectCode, contentCode);
    }

    private List<Optional<Future<Void>>> sendEmailToNegTeamMembers(Negotiation neg, String subjectCode, String contentCode) {
        String url = getCustomerNegotiationUrl(neg);
        Map<String, String> parameters = getNotificationMessageParameters(neg, url);
        String subject = messages.getFromDb(subjectCode, "RU", parameters);
        String content = messages.getFromDb(contentCode, "RU");
        return neg.getNegTeam().stream()
                .map(member -> sendEmailToNegTeamMember(subject, content, parameters, member))
                .collect(toList());
    }

    private List<Optional<Future<Void>>> sendEmailToSupplierEmployees(Negotiation neg, List<Employee> supplierEmployees, String subjectCode, String contentCode) {
        String url = getSupplierNegotiationUrl(neg);
        Map<String, String> parameters = getNotificationMessageParameters(neg, url);
        String subject = messages.getFromDb(subjectCode, "RU", parameters);
        String content = messages.getFromDb(contentCode, "RU");
        return supplierEmployees.stream()
                .map(employee -> sendEmailToEmployee(subject, content, parameters, employee))
                .collect(toList());
    }


    private Optional<Future<Void>> sendEmailToNegTeamMember(String subject, String content, Map<String, String> params, NegTeam member) {
        Employee employee = employeeService.findByUserId(member.getUserId());
        return sendEmailToEmployee(subject, content, params, employee);
    }

    private Optional<Future<Void>> sendEmailToEmployee(String subject, String content, Map<String, String> params, Employee employee) {
        if (employee.getEmail() == null) {
            logger.warn("email is empty: userId={}", employee.getUserId());
            return Optional.empty();
        }
        params.put("RECIPIENT_NAME", employee.getFullName());
        StrSubstitutor ss = new StrSubstitutor(params);
        logger.debug("sending email to: email={}", employee.getEmail());
        try {
            Future<Void> future = mailService.sendMail(employee.getEmail(), subject, ss.replace(content));
            return Optional.of(future);
        } catch (MessagingException e) {
            throw new NegotiationNotificationSendingException();
        }
    }

    private String getCustomerNegotiationUrl(Negotiation neg) {
        return conf.getHost() + "/customer.html#negotiations/" + neg.getNegId();
    }

    private String getSupplierNegotiationUrl(Negotiation neg) {
        return conf.getHost() + "/supplier.html#negotiation/" + neg.getNegId();
    }

    private Map<String, String> getNotificationMessageParameters(Negotiation neg, String negUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("DOCUMENT_NUMBER", neg.getDocNumber());
        params.put("AUCTION_TITLE", neg.getTitle());
        params.put("NEG_URL", negUrl);
        return params;
    }
}
