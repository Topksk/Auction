package com.bas.auction.profile.customer.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.dto.Customer;

import java.util.Map;

public interface CustomerDAO {

	Customer findUserOrg(User user);

	String findRegStatus(Long customerId);

	String findBin(Long customerId);

	Long findIdByBin(String bin);

	Customer findByIdForUser(User user, Long customerId);

	boolean exists(String bin);

	Customer insert(User user, Customer data);

	Customer update(User user, Customer data);

	Customer updateRegStatus(Long userId, Long customerId, String approved);

	Map<String, Object> findForIntegra(long customerId);

	void setSent(Long negId);
}
