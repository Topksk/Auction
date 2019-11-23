package com.bas.auction.auth.dao.impl;

import com.bas.auction.auth.dao.SessionDAO;
import com.bas.auction.auth.dto.Session;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class SessionDAOImpl implements SessionDAO, GenericDAO<Session> {
    private final UserService userService;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public SessionDAOImpl(DaoJdbcUtil daoutil, UserService userService) {
        this.daoutil = daoutil;
        this.userService = userService;
    }

    @Override
    public String getSqlPath() {
        return "auth/session";
    }

    @Override
    public Class<Session> getEntityType() {
        return Session.class;
    }

    @Override
    public Session findByToken(String token) {
        Session session = daoutil.queryForObject(this, "get_by_token", token);
        if (session != null && session.getUserId() != null)
            session.setUser(userService.findById(session.getUserId()));
        return session;
    }

    @Override
    public Long sessionIdByUser(long userId) {
        long sessionId = daoutil.queryScalar(this, "get_session_id_by_userid", userId);
        return sessionId;
    }

    @Override
    public void set_session_id (Object[] params){
        daoutil.queryForMapList("auth/session/set_session_id", params);
    }


    @Override
    @SpringTransactional
    public Session create(Session data) {
        Object[] values = {data.getUserId(), data.getAuthToken(), data.getUserAgent(), data.getIpAddress(),
                data.getUserId(), data.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        data.setSessionId((Long) kh.getKeys().get("session_id"));
        return data;
    }

    @Override
    @SpringTransactional
    public void deactivateUserSessions(Long userId) {
        daoutil.dml(this, "deactivate_user_sessions", new Object[]{userId, userId});
    }
}
