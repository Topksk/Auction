package com.bas.auction.core.config.security;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsManagerImpl implements UserDetailsManager, GenericDAO<User> {
    private final DaoJdbcUtil daoJdbcUtil;
    private final static Logger logger = LoggerFactory.getLogger(UserDetailsManagerImpl.class);

    @Autowired
    public UserDetailsManagerImpl(DaoJdbcUtil daoJdbcUtil) {
        this.daoJdbcUtil = daoJdbcUtil;
    }

    @Override
    public Class<User> getEntityType() {
        return User.class;
    }

    @Override
    public String getSqlPath() {
        return "user";
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return daoJdbcUtil.exists(this, "user_exists", username);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("paramete22222222={}");

       String code=daoJdbcUtil.queryScalar(this, "get_det_log", username);

            if (code.equals("3")){
                return daoJdbcUtil.queryForObject(this, "get_by_login_sn", username);
            }else{
        return daoJdbcUtil.queryForObject(this, "get_by_login", username);
            }

    }
}
