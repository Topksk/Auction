package com.bas.auction.profile.customer.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.customer.dto.Customer;

import java.io.IOException;

public interface CustomerService {
    Customer create(User user, Customer data);

    Customer approveCustomer(Long userId, Long customerId) throws IOException;

    Customer confirmRegistration(Long userId, Long customerId);
}
