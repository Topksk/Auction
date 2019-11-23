package com.bas.auction.auth.dao;

import com.bas.auction.auth.dto.OneTimeCode;

public interface OneTimeCodeDAO {
	OneTimeCode findOneTimeCode(String code);

	OneTimeCode create(Long userId);

	void delete(String code);
}
