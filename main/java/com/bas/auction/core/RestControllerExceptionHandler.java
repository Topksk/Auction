package com.bas.auction.core;


import com.bas.auction.core.crypto.CertValidationError;
import com.bas.auction.core.dao.DaoException;
import com.bas.auction.core.dao.MessageDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RestControllerExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(RestControllerExceptionHandler.class);
    private final MessageDAO messageDAO;

    public RestControllerExceptionHandler(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, List<String>>> exception(Exception ex) {
        logger.error("Exception", ex);
        List<String> message = Collections.singletonList(messageDAO.get("INTERNAL_SERVER_ERROR"));
        Map<String, List<String>> messages = Collections.singletonMap("messages", message);
        return new ResponseEntity<>(messages, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DaoException.class)
    public ResponseEntity<Map<String, String>> daoException(DaoException ex) {
        logger.error("Exception", ex);
        String message = messageDAO.get(ex.getCode());
        Map<String, String> messages = Collections.singletonMap("messages", message);
        return new ResponseEntity<>(messages, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CertValidationError.class)
    public ResponseEntity<Map<String, String>> certValidationException(CertValidationError ex) {
        logger.error("Exception", ex);
        String errorCode = ex.getType().toString();
        String message = messageDAO.get(errorCode);
        Map<String, String> messages = Collections.singletonMap("messages", message);
        return new ResponseEntity<>(messages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApplException.class)
    public ResponseEntity<Map<String, List<String>>> applException(ApplException ex) {
        logger.error("Exception", ex);
        HttpStatus httpStatus;
        if (ex.getStatusCode() > 0)
            httpStatus = HttpStatus.valueOf(ex.getStatusCode());
        else
            httpStatus = HttpStatus.CONFLICT;
        Map<String, List<String>> messages = Collections.singletonMap("messages", getMessage(ex));
        return new ResponseEntity<>(messages, httpStatus);
    }

    private List<String> getMessage(ApplException ex) {
        List<String> list = new ArrayList<>();
        boolean hasParams = ex.getParams() != null;
        for (String code : ex.getCodes()) {
            if (hasParams) {
                List<String> messages = ex.getParams().stream()
                        .map(param -> messageDAO.get(code, param))
                        .collect(Collectors.toList());
                list.addAll(messages);
            } else
                list.add(messageDAO.get(code));
        }
        return list;
    }
}
