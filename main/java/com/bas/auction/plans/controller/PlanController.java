package com.bas.auction.plans.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.plans.PlanTemplateGenerator;
import com.bas.auction.plans.dao.PlanDAO;
import com.bas.auction.plans.dto.Plan;
import com.bas.auction.plans.service.PlanService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/plans", produces = APPLICATION_JSON_UTF8_VALUE)
public class PlanController extends RestControllerExceptionHandler {
    private final PlanDAO planDAO;
    private final PlanService planService;
    private final PlanTemplateGenerator planTemplateGenerator;

    @Autowired
    public PlanController(MessageDAO messageDAO, PlanDAO planDAO, PlanService planService, PlanTemplateGenerator planTemplateGenerator) {
        super(messageDAO);
        this.planDAO = planDAO;
        this.planService = planService;
        this.planTemplateGenerator = planTemplateGenerator;
    }

    @RequestMapping(path = "/{planId}", method = GET)
    public Plan findPlan(@PathVariable long planId, @CurrentUser User user) {
        MDC.put("action", "find plan");
        return planDAO.findPlan(user, planId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Plan create(@RequestBody Plan plan,
                       @CurrentUser User user) {
        MDC.put("action", "create plan");
        plan.setCustomerId(user.getCustomerId());
        return planService.create(user, plan);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = PUT, params = "approve", consumes = APPLICATION_JSON_UTF8_VALUE)
    public Plan createApprove(@RequestBody Plan plan,
                              @CurrentUser User user) {
        MDC.put("action", "create approve plan");
        plan = create(plan, user);
        List<Long> selectedPlans = Collections.singletonList(plan.getPlanId());
        return planService.approve(user, selectedPlans);
    }

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Plan save(@RequestBody Plan plan,
                     @CurrentUser User user) {
        MDC.put("action", "save plan");
        plan.setCustomerId(user.getCustomerId());
        return planService.update(user, plan);
    }

    @RequestMapping(method = POST, params = "approve", consumes = APPLICATION_JSON_UTF8_VALUE)
    public Plan approve(@RequestBody Plan plan,
                        @CurrentUser User user) {
        MDC.put("action", "approve plan");
        plan = save(plan, user);
        List<Long> selectedPlans = Collections.singletonList(plan.getPlanId());
        return planService.approve(user, selectedPlans);
    }

    @RequestMapping(method = POST, params = "approveSelected", consumes = APPLICATION_JSON_UTF8_VALUE)
    public void approvePlans(@RequestBody List<Long> selectedPlans,
                             @CurrentUser User user) {
        MDC.put("action", "approve plans");
        planService.approve(user, selectedPlans);
    }

    @RequestMapping(method = DELETE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void delete(@RequestBody List<Long> selectedPlans,
                       @CurrentUser User user) {
        MDC.put("action", "delete plan");
        planService.delete(selectedPlans);
    }

    @RequestMapping(path = "/templates/{settingId}")
    public void downloadTemplate(@PathVariable long settingId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        MDC.put("action", "download plan template");
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String contentDisposition = "plan_template.xlsx";
        String uagent = req.getHeader("User-Agent");
        if (uagent.contains("MSIE"))
            contentDisposition = "Attachment; Filename=" + contentDisposition;
        else
            contentDisposition = "Attachment; Filename*=UTF-8''" + contentDisposition;
        resp.setHeader("Content-disposition", contentDisposition);
        try (BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream())) {
            planTemplateGenerator.generateTemplate(settingId, out);
        }
    }
}