package com.bas.auction.profile.supplier.setting.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.supplier.dto.SupplierSetting;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/supplierProfile/settings", produces = APPLICATION_JSON_UTF8_VALUE)
public class SupplierSettingsController extends RestControllerExceptionHandler {
    private final SupplierSettingDAO supplierSettingDAO;

    @Autowired
    public SupplierSettingsController(MessageDAO messageDAO, SupplierSettingDAO supplierSettingDAO) {
        super(messageDAO);
        this.supplierSettingDAO = supplierSettingDAO;
    }

    @RequestMapping(path = "/current", method = GET)
    public SupplierSetting findCurrentSupplierSetting(@CurrentUser User user) {
        MDC.put("action", "find supplier setting");
        return supplierSettingDAO.findMainSupplierSetting(user.getSupplierId());
    }

    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public SupplierSetting create(@RequestBody SupplierSetting setting,
                                  @CurrentUser User user) {
        MDC.put("action", "create supplier setting");
        supplierSettingDAO.create(user, setting);
        return supplierSettingDAO.findMainSupplierSetting(user.getSupplierId());
    }

    @RequestMapping(path = "/{settingId}", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public SupplierSetting save(@PathVariable long settingId,
                                @RequestBody SupplierSetting setting,
                                @CurrentUser User user) {
        MDC.put("action", "save supplier setting");
        setting.setSettingId(settingId);
        supplierSettingDAO.update(user, setting);
        return supplierSettingDAO.findMainSupplierSetting(user.getSupplierId());
    }
}
