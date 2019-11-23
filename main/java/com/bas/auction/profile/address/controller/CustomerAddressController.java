package com.bas.auction.profile.address.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/customerProfile/addresses", produces = APPLICATION_JSON_UTF8_VALUE)
public class CustomerAddressController extends RestControllerExceptionHandler {
    private final AddressService addressService;

    @Autowired
    public CustomerAddressController(MessageDAO messageDAO, AddressService addressService) {
        super(messageDAO);
        this.addressService = addressService;
    }

    @RequestMapping(method = GET)
    public List<Address> findAddresses(@CurrentUser User user) {
        MDC.put("action", "find addresses");
        return addressService.findCustomerAddresses(user.getCustomerId());
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Address> create(@RequestBody Address address,
                                @CurrentUser User user) {
        MDC.put("action", "create address");
        address.setSupplierId(null);
        if (!user.isSysadmin())
            address.setCustomerId(user.getCustomerId());
        Long customerId = address.getCustomerId();
        addressService.create(user, address);
        return addressService.findCustomerAddresses(customerId);
    }

    @RequestMapping(path = "/{addressId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Address> save(@PathVariable long addressId,
                              @RequestBody Address address,
                              @CurrentUser User user) {
        MDC.put("action", "save address");
        address.setSupplierId(null);
        address.setAddressId(addressId);
        if (!user.isSysadmin())
            address.setCustomerId(user.getCustomerId());
        Long customerId = address.getCustomerId();
        addressService.update(user, address);
        return addressService.findCustomerAddresses(customerId);
    }
}
