package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SessionCreateDAO {
    String REMEMBER_ME_COOKIE_KEY = "x-auth-token";

    void createSession (long userId, HttpServletRequest request, HttpServletResponse response);

    void set_var (long userId);

}
