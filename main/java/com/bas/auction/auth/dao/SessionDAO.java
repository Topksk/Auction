package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.Session;

public interface SessionDAO {
    String REMEMBER_ME_COOKIE_KEY = "x-auth-token";

    Session findByToken(String token);

    Long sessionIdByUser(long userId);

    void set_session_id (Object[] params);

    Session create(Session data);

    void deactivateUserSessions(Long userId);
}
