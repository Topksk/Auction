package com.bas.auction.profile.supplier.service.impl;

import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.service.SupplierNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupplierNotificationServiceImpl implements SupplierNotificationService {
    private final Logger logger = LoggerFactory.getLogger(SupplierNotificationServiceImpl.class);
    private final Conf conf;
    private final EmployeeService employeeService;
    private final MessageDAO messages;
    private final MailService mailService;

    @Autowired
    public SupplierNotificationServiceImpl(Conf conf, EmployeeService employeeService, MessageDAO messages, MailService mailService) {
        this.conf = conf;
        this.employeeService = employeeService;
        this.messages = messages;
        this.mailService = mailService;
    }

    protected void sendMail(String email, String subjectCode, String bodyCode, Map<String, String> params) {
        String subject = messages.getFromDb(subjectCode, "RU");
        String body;
        if (params == null)
            body = messages.getFromDb(bodyCode, "RU");
        else
            body = messages.getFromDb(bodyCode, "RU", params);
        try {
            mailService.sendMail(email, subject, body);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendRegStatusMail(long supplierId, boolean approved, String rejectReason) {
        if (approved)
            logger.debug("send supplier reg approve mail: {}", supplierId);
        else
            logger.debug("send supplier reg reject mail: {}", supplierId);
        Employee emp = employeeService.findSupplierMainEmployee(supplierId);
        if (emp.getEmail() == null) {
            logger.error("no supplier email for registration status notification: {}", supplierId);
            return;
        }
        String instUrl = conf.getInstructionsUrl();
        Map<String, String> params = new HashMap<>();
        params.put("RECIPIENT_NAME", emp.getFullName());
        params.put("INSTRUCTIONS_URL", instUrl);
        String subjectCode;
        String bodyCode;
        if (approved) {
            subjectCode = "SUPPLIER_REG_APPROVED_SUBJECT";
            bodyCode = "SUPPLIER_REG_APPROVED_BODY";
        } else {
            params.put("REJECT_REASON", rejectReason);
            subjectCode = "SUPPLIER_REG_REJECT_SUBJECT";
            bodyCode = "SUPPLIER_REG_REJECT_BODY";
        }
        logger.debug("sending supplier reg mail to : {}", emp.getEmail());
        sendMail(emp.getEmail(), subjectCode, bodyCode, params);
    }
}
