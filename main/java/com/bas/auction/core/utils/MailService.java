package com.bas.auction.core.utils;

import com.bas.auction.core.Conf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {
    private final Conf conf;
    private final Session session;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    public MailService(Conf conf, Session session) {
        this.conf = conf;
        this.session = session;
    }

    @Async("mailTaskExecutor")
    public Future<Void> sendMail(String recipient, String subject, String content) throws MessagingException {
        Thread.currentThread().setName("mail sender");
        String from = conf.getMailSendFrom();
        String password = conf.getMailSenderPassword();
        sendMail(from, password, new String[]{recipient}, subject, content);
        return new AsyncResult<>(null);
    }

    @Async("mailTaskExecutor")
    public Future<Void> sendMail(String[] recipients, String subject, String content) throws MessagingException {
        Thread.currentThread().setName("mail sender");
        String from = conf.getMailSendFrom();
        String password = conf.getMailSenderPassword();
        sendMail(from, password, recipients, subject, content);
        return new AsyncResult<>(null);
    }

    private void sendMail(String from, String password, String[] recipients, String subject, String content) throws MessagingException {
        String host = conf.getSmtpHost();
        MimeMessage msg = getMimeMessage(from, recipients, subject, content);
        Transport t = session.getTransport("smtps");
        try {
            t.connect(host, from, password);
            t.sendMessage(msg, msg.getAllRecipients());
        } catch (MessagingException e) {
            logger.error("MailService.sendMail.Error");
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("MailService.sendMail, from=" + from);
            for(String s1:recipients){
                logger.info("MailService.sendMail, to="+s1);
            }
            t.close();
        }
    }

    private MimeMessage getMimeMessage(String from, String[] recipients, String subject, String content) throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(subject);
        msg.setFrom(from);
        InternetAddress[] addresses = getRecipientsList(recipients);
        msg.addRecipients(RecipientType.TO, addresses);
        msg.setContent(content, conf.getMailContentType());
        return msg;
    }

    private InternetAddress[] getRecipientsList(String[] recipients) throws AddressException {
        InternetAddress[] addresses = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++)
            addresses[i] = new InternetAddress(recipients[i]);
        return addresses;
    }
}
