package com.bas.auction.search.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.SpravService;
import com.bas.auction.bid.search.service.BidSearchService;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.lookup.dto.LookupResults;
import com.bas.auction.lookup.search.service.LookupSearchService;
import com.bas.auction.neg.search.service.NegSearchService;
import com.bas.auction.plans.search.PlanSearchService;
import com.bas.auction.profile.customer.service.CustomerSearchService;
import com.bas.auction.profile.request.dto.ReqSearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.bas.auction.profile.request.dto.AddressSearch;
import com.bas.auction.profile.request.dto.KskEmpsSearch;

@RestController
@RequestMapping(path = "/search", produces = APPLICATION_JSON_UTF8_VALUE)
public class SearchController extends RestControllerExceptionHandler {
    private final LookupSearchService lookupSearchService;
    private final NegSearchService negSearchService;
    private final BidSearchService bidSearchService;
    private final PlanSearchService planSearchService;
    private final CustomerSearchService customerSearchService;
    //private final SupplierSearchService supplierSearchService;
    //private final RequestSearchService requestSearchService;
    private final static Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SpravService spravService;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public SearchController(MessageDAO messageDAO, LookupSearchService lookupSearchService, NegSearchService negSearchService, BidSearchService bidSearchService, PlanSearchService planSearchService, CustomerSearchService customerSearchService, SpravService spravService, DaoJdbcUtil daoutil) {
        super(messageDAO);
        this.lookupSearchService = lookupSearchService;
        this.negSearchService = negSearchService;
        this.bidSearchService = bidSearchService;
        this.planSearchService = planSearchService;
        this.customerSearchService = customerSearchService;
        //this.supplierSearchService = supplierSearchService;
        //this.requestSearchService = requestSearchService;
        this.spravService = spravService;
        this.daoutil = daoutil;
    }

    @RequestMapping(path = "/kato", method = GET)
    public LookupResults quickSearchKato(@RequestParam String search) {
        return lookupSearchService.searchKato(search);
    }

    @RequestMapping(path = "/enstru", method = {GET, POST})
    public LookupResults quickSearchEnstru(@RequestParam String search) {
        return lookupSearchService.searchEnstru(search);
    }

    @RequestMapping(path = "/skp", method = {GET, POST})
    public LookupResults quickSearchSkp(@RequestParam String search) {
        return lookupSearchService.searchSkp(search);
    }

    @RequestMapping(path = "/enstru/list", method = {POST})
    public LookupResults searchEnstru(@RequestParam String search) {
        return lookupSearchService.searchEnstruBit(search);
    }

    @RequestMapping(path = "/skp/list", method = {POST})
    public LookupResults searchSkp(@RequestParam String search) {
        return lookupSearchService.searchSkpBit(search);
    }

    @RequestMapping(path = "/uom", method = GET)
    public LookupResults quickSearchUom(@RequestParam String search) {
        return lookupSearchService.searchUom(search);
    }

    @RequestMapping(path = "/city", method = GET)
    public LookupResults quickSearchCity(@RequestParam String search) {
        return lookupSearchService.searchCity(search);
    }

    @RequestMapping(path = "/plans", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> searchPlans(@RequestBody Map<String, Object> data,
                                                 @RequestParam(required = false) String withUomDesc,
                                                 @CurrentUser User user) throws ParseException {
        data.put("customer_id", user.getCustomerId());
        return planSearchService.searchPlans(data, "Y".equalsIgnoreCase(withUomDesc));
    }


    @RequestMapping(path = "/customers", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> searchCustomers(@RequestBody Map<String, Object> data, @CurrentUser User user) {
        return customerSearchService.searchCustomers(data);
    }


    @RequestMapping(path = "/reqs", method = POST/*, consumes = APPLICATION_JSON_UTF8_VALUE*/)
    public List<Map<String, Object>> searchReqs(@RequestBody Map<String, String> params, @CurrentUser User user) {
        MDC.put("action", "searchReqs");
        logger.info("params.str = " + params.toString());
        logger.info("params.size = " + params.size());

        ReqSearch new_par = new ReqSearch();
        new_par.setId(params.get("id"));
        new_par.setReq_city(params.get("req_city"));
        logger.info("getReq_city() = " + new_par.getReq_city());
        new_par.setLang_id(params.get("lang_id"));
        logger.info("getLang_id() = " + new_par.getLang_id());
        if(params.containsKey("ksk_id")){
        new_par.setKsk_id(params.get("ksk_id"));
        logger.info("setKsk_id() = " + new_par.getKsk_id());
        }
        if (params.containsKey("userId")){
            new_par.setUser_id(user.getUserId().toString());
            logger.info("getUser_id() = " + new_par.getUser_id());
        }

        if(params.containsKey("comp_id")){
            new_par.setComp_id(params.get("comp_id"));
            logger.info("setComp_id() = " + new_par.getComp_id());
        }

        if (new_par.getId()!=null) {
            logger.info("getId() = " + new_par.getId());
        }
        else {
            new_par.setDat_reg_beg(params.get("dat_reg_beg"));
            logger.info("getDat_reg_beg() = " + new_par.getDat_reg_beg());
            new_par.setDat_reg_end(params.get("dat_reg_end"));
            logger.info("getDat_reg_end() = " + new_par.getDat_reg_end());
            new_par.setReq_street(params.get("req_street"));
            logger.info("getReq_street() = " + new_par.getReq_street());
            new_par.setReq_building(params.get("req_building"));
            logger.info("getReq_building() = " + new_par.getReq_building());
            new_par.setT_flats_id(params.get("t_flats_id"));
            logger.info("getT_flats_id() = " + new_par.getT_flats_id());
            new_par.setReq_type(params.get("req_type"));
            logger.info("getReq_type() = " + new_par.getReq_type());
            /*new_par.setT_req_subtype_id(params.get("t_req_subtype_id"));
            logger.info("getT_req_subtype_id() = " + new_par.getT_req_subtype_id());*/
            new_par.setT_req_priority_id(params.get("t_req_priority_id"));
            logger.info("getT_req_priority_id() = " + new_par.getT_req_priority_id());
            new_par.setReq_status(params.get("req_status"));
            logger.info("getReq_status() = " + new_par.getReq_status());
            new_par.setReq_disp_exec(params.get("req_disp_exec"));
            logger.info("getReq_disp_exec() = " + new_par.getReq_disp_exec());
            new_par.setReq_executer(params.get("req_executer"));
            logger.info("getReq_executer() = " + new_par.getReq_executer());



        }

        if (params.containsKey("citreqs")){
            return spravService.findCitReqs(new_par);
        }else if(params.containsKey("reqserv")){
            return spravService.findReqserv(new_par);
        }
        else{
            return spravService.findReqs(new_par);
        }

    }

    @RequestMapping(path = "/kskAddress", method = POST/*, consumes = APPLICATION_JSON_UTF8_VALUE*/)
    public List<Map<String, Object>> searchKsk(@RequestBody Map<String, String> params) {
        MDC.put("action", "searchReqs");
        logger.info("params.str = " + params.toString());
        logger.info("params.size = " + params.size());

        AddressSearch new_par = new AddressSearch();
        new_par.setLangId(params.get("lang_id"));
        new_par.setId(params.get("id"));
        //logger.info("lang_id() = " + params.get("lang_id"));
        new_par.setCity(params.get("city"));
        new_par.setStreet(params.get("street"));
        new_par.setBuilding(params.get("building"));
        new_par.setFraction(params.get("fraction"));
        new_par.setPhone(params.get("phone"));
        new_par.setDisp(params.get("disp"));
		new_par.setStatus(params.get("status"));
        //logger.info("getDisp() = " + new_par.getDisp());
        return spravService.findKskAddress(new_par);
    }

    @RequestMapping(path = "/addressList", method = POST/*, consumes = APPLICATION_JSON_UTF8_VALUE*/)
    public List<Map<String, Object>> searchAddressList(@RequestBody Map<String, String> params) {
        MDC.put("action", "searchReqs");
        logger.info("params.str = " + params.toString());
        logger.info("params.size = " + params.size());

        AddressSearch new_par = new AddressSearch();
        new_par.setLangId(params.get("lang_id"));
        //logger.info("lang_id() = " + params.get("lang_id"));
        new_par.setCity(params.get("city"));
        new_par.setStreet(params.get("street"));
        new_par.setBuilding(params.get("building"));
        new_par.setFraction(params.get("fraction"));
        //logger.info("getDisp() = " + new_par.getDisp());
        return spravService.findAddressList(new_par);
    }


    @RequestMapping(path = "/ksks", method = POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public List<Map<String, Object>> kskSearch(@RequestBody Map<String, Object> data, @CurrentUser User user) {
        logger.info("other,"+data.toString()+",user="+user.toString());
        List<Map<String, Object>> res;
        //logger.info();
        Object[] newParams=new Object[3];
        int nn=0;
        newParams[nn] = data.get("lang_id");
        nn++;
        newParams[nn] = "%" + data.get("bin") + "%";
        nn++;
        newParams[nn] = "%" + data.get("kskname") + "%";
        String sqlcode= (String) data.get("sqlpath");
        res=daoutil.queryForMapList(sqlcode, newParams);
        //logger.info(res.toString());
        return res;
    }

    @RequestMapping(path = "/kskEmpsList", method = POST/*, consumes = APPLICATION_JSON_UTF8_VALUE*/)
    public List<Map<String, Object>> searchKskEmpsList(@RequestBody Map<String, String> params) {
        MDC.put("action", "searchKskEmpsList");
        logger.info("params.str = " + params.toString());
        logger.info("params.size = " + params.size());

        KskEmpsSearch new_par = new KskEmpsSearch();
        if(params.containsKey("id")) {
        new_par.setLangId(params.get("lang_id"));
        new_par.setId(params.get("id"));
        //logger.info("lang_id() = " + params.get("lang_id"));
        new_par.setLastName(params.get("lastName"));
        new_par.setFirstName(params.get("firstName"));
        new_par.setMiddleName(params.get("middleName"));
        new_par.setPhone(params.get("phone"));
        new_par.setPriznPos(params.get("position_types"));
		new_par.setStatus(params.get("status"));
        logger.info("getPriznPos() = " + new_par.getPriznPos());
        return spravService.findKskEmpsList(new_par);
        }else{
            new_par.setLangId(params.get("lang_id"));
            new_par.setId(params.get("comp_id"));
            //logger.info("lang_id() = " + params.get("lang_id"));
            new_par.setLastName(params.get("lastName"));
            new_par.setFirstName(params.get("firstName"));
            new_par.setMiddleName(params.get("middleName"));
            new_par.setPhone(params.get("phone"));
            new_par.setPriznPos(params.get("position_types"));
            new_par.setStatus(params.get("status"));
            logger.info("getPriznPos2() = " + new_par.getPriznPos());
            return spravService.findServEmpsList(new_par);
        }
    }

}
