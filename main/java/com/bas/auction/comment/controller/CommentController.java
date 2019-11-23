package com.bas.auction.comment.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.comment.dao.CommentDAO;
import com.bas.auction.comment.dto.Comment;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.profile.employee.dto.Person;
import com.bas.auction.profile.employee.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(path = "/comments", produces = APPLICATION_JSON_UTF8_VALUE)
public class CommentController extends RestControllerExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentDAO commentDAO;
    private final NegotiationDAO negotiationDAO;
    private final MailService mailService;
    private final MessageDAO messagesDAO;
    private final UserService userService;
    private final PersonService personService;
    private final Conf conf;

    @Autowired
    public CommentController(CommentDAO commentDAO, NegotiationDAO negotiationDAO, MailService mailService, MessageDAO messagesDAO, UserService userService, PersonService personService, Conf conf) {
        super(messagesDAO);
        this.commentDAO = commentDAO;
        this.negotiationDAO = negotiationDAO;
        this.mailService = mailService;
        this.messagesDAO = messagesDAO;
        this.userService = userService;
        this.personService = personService;
        this.conf = conf;
    }

    @RequestMapping(path = "/negs/{negId}", method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Comment comment(@PathVariable long negId, @RequestBody Comment comment, @CurrentUser User user) {
        Negotiation neg = negotiationDAO.findNotDraftNeg(user, comment.getNegId());
        if (neg == null)
            throw new AccessDeniedException();
        boolean isCustomer = user.isCustomer() && user.getCustomerId() == neg.getCustomerId();
        comment.setIsCustomer(isCustomer);
        comment = commentDAO.create(user, comment);
        if (comment.isCustomer()) {
            Comment parent = commentDAO.findById(comment.getParentId());
            if (parent != null && !user.getUserId().equals(parent.getCreatedBy())) {
                sendEmailToUser(parent.getCreatedBy(), "NEG_COMMENT_ANSWERED", comment, neg);
            }
        } else {
            sendEmailToUser(neg.getCreatedBy(), "NEG_COMMENT_NEW", comment, neg);
        }
        return comment;
    }

    private void sendEmailToUser(long userId, String messagePrefix, Comment comment, Negotiation neg) {
        try {
            User recipient = userService.findById(userId);
            Person person = personService.findById(recipient.getPersonId());
            String userType = !comment.isCustomer() ? "customer" : "supplier";

            String subject = messagesDAO.getFromDb(messagePrefix + "_SUBJECT", "RU");
            Map<String, String> params = new HashMap<>();
            params.put("NEG_TITLE", neg.getTitle());
            params.put("NEG_ID", Long.toString(neg.getNegId()));
            params.put("TEXT", comment.getText());
            String actUrl = conf.getHost() + "/" + userType + ".html#negotiations/" + comment.getNegId();
            params.put("LINK", actUrl);
            params.put("NAME", person.getFullName());
            String content = messagesDAO.getFromDb(messagePrefix + "_BODY", "RU", params);

            mailService.sendMail(recipient.getEmail(), subject, content);
        } catch (MessagingException e) {
            logger.error(e.getMessage(), e);
        }
    }
}