package com.bas.auction.profile.customer.setting.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/customerProfile/settings", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CustomerSettingsController extends RestControllerExceptionHandler {
    private final CustomerSettingDAO customerSettingsDAO;

    @Autowired
    public CustomerSettingsController(MessageDAO messageDAO, CustomerSettingDAO customerSettingsDAO) {
        super(messageDAO);
        this.customerSettingsDAO = customerSettingsDAO;
    }

    @RequestMapping(params = "main", method = RequestMethod.GET)
    public CustomerSetting findMainSetting(@CurrentUser User user) {
        MDC.put("action", "find main setting");
        return customerSettingsDAO.findMainWithDetails(user.getCustomerId());
    }

    @RequestMapping(path = "/{settingId}", method = RequestMethod.GET)
    public CustomerSetting findSetting(@PathVariable Long settingId) {
        MDC.put("action", "find setting");
        return customerSettingsDAO.findByIdWithDetails(settingId);
    }

    @RequestMapping(path = "/", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CustomerSetting> create(@RequestBody Map<String, Object> param,
                                        @CurrentUser User user) {
        String settingName = (String) param.get("name");
        long customerId = ((Number) param.get("customer_id")).longValue();
        customerSettingsDAO.create(user, customerId, settingName);
        return customerSettingsDAO.findCustomerSettings(customerId);
    }

    @RequestMapping(path = "/{settingId}", params = "save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CustomerSetting> save(@PathVariable Long settingId,
                                      @RequestBody CustomerSetting customerSetting,
                                      @CurrentUser User user) {
        customerSettingsDAO.update(user, customerSetting);
        return customerSettingsDAO.findCustomerSettings(customerSetting.getCustomerId());
    }

    @RequestMapping(path = "/{settingId}", params = "make_main", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<CustomerSetting> mainSettings(@PathVariable Long settingId,
                                              @RequestBody CustomerSetting customerSetting,
                                              @CurrentUser User user) {
        customerSettingsDAO.update(user, customerSetting);
        customerSettingsDAO.makeMain(user.getUserId(), settingId, customerSetting.getCustomerId());
        return customerSettingsDAO.findCustomerSettings(customerSetting.getCustomerId());
    }
}
