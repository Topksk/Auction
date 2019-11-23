package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.OneTimeCodeForEmail;

public interface OneTimeCodeDAOEmail {

    OneTimeCodeForEmail findOneTimeCodeEmail(String code);

    OneTimeCodeForEmail createCode(String email);

    void delete(String code);
}