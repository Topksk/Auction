package com.bas.auction.profile.address.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/supplierProfile/addresses", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierAddressController extends RestControllerExceptionHandler {
    private final AddressService addressService;
    private final SupplierDAO supplierDAO;
    @Autowired
    public SupplierAddressController(MessageDAO messageDAO, AddressService addressService, SupplierDAO supplierDAO) {
        super(messageDAO);
        this.addressService = addressService;
        this.supplierDAO = supplierDAO;
    }

    @RequestMapping(method = GET)
    public Map<String, Object> findAddresses(@CurrentUser User user) {
        MDC.put("action", "find addresses");
        return findAddresses(user.getSupplierId());
    }

    @ResponseStatus(CREATED)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> create(@RequestBody Address address,
                                      @CurrentUser User user) {
        MDC.put("action", "create address");
        address.setCustomerId(null);
        if (!user.isSysadmin())
            address.setSupplierId(user.getSupplierId());
        Long supplierId = address.getSupplierId();
        addressService.create(user, address);
        return findAddresses(supplierId);
    }

    @RequestMapping(path = "/{addressId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> save(@PathVariable long addressId,
                                    @RequestBody Address address,
                                    @CurrentUser User user) {
        MDC.put("action", "save address");
        address.setCustomerId(null);
        address.setAddressId(addressId);
        if (!user.isSysadmin())
            address.setSupplierId(user.getSupplierId());
        Long supplierId = address.getSupplierId();
        addressService.update(user, address);
        return findAddresses(supplierId);
    }

    private Map<String, Object> findAddresses(Long supplierId) {
        String country = supplierDAO.findSupplierCountry(supplierId);
        Map<String, Object> res = new HashMap<>();
        res.put("addresses", addressService.findSupplierAddresses(supplierId));
        res.put("country", country);
        return res;
    }
}
