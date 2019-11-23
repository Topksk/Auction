package com.bas.auction.profile.supplier.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.service.SupplierService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(path = "/supplierProfile/organizations", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierProfileController extends RestControllerExceptionHandler {
    private final SupplierDAO supplierDAO;
    private final SupplierService supplierService;

    @Autowired
    public SupplierProfileController(MessageDAO messageDAO, SupplierDAO supplierDAO, SupplierService supplierService) {
        super(messageDAO);
        this.supplierDAO = supplierDAO;
        this.supplierService = supplierService;
    }

    @RequestMapping(path = "/current", method = GET)
    public Supplier findUserOrg(@CurrentUser User user) {
        MDC.put("action", "find current org");
        return supplierDAO.findUserOrg(user);
    }

    @RequestMapping(path = "/{supplierId}", params = "save", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Supplier save(@PathVariable long supplierId,
                         @RequestBody Supplier supplier,
                         @CurrentUser User user) {
        MDC.put("action", "save supplier");
        if (!user.isSysadmin())
            supplierId = user.getSupplierId();
        supplier.setSupplierId(supplierId);
        return supplierDAO.update(user, supplier);
    }

    @RequestMapping(path = "/{supplierId}", params = "confirm_registration", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Supplier confirmRegistration(@PathVariable long supplierId,
                                        @RequestBody Supplier supplier,
                                        @CurrentUser User user) {
        MDC.put("action", "confirm supplier reg");
        if (!user.isSysadmin())
            supplierId = user.getSupplierId();
        supplier.setSupplierId(supplierId);
        supplierDAO.update(user, supplier);
        return supplierService.confirmRegistration(user, supplierId);
    }

    @RequestMapping(path = "/{supplierId}", params = "send_to_approve", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Supplier sendToApprove(@PathVariable long supplierId,
                                  @RequestBody Supplier supplier,
                                  @CurrentUser User user) throws IOException {
        MDC.put("action", "approve supplier");
        if (!user.isSysadmin())
            supplierId = user.getSupplierId();
        supplier.setSupplierId(supplierId);
        supplierDAO.update(user, supplier);
        return supplierService.sendForApproval(user, supplierId);
    }
}
