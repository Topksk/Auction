package com.bas.auction.req.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.req.dao.RequestDAO;
import com.bas.auction.req.dto.Request;
import com.bas.auction.req.dto.ReqHistory;
import com.bas.auction.auth.service.UserService;
import org.apache.commons.collections.map.HashedMap;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.bas.auction.core.Conf;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import com.bas.auction.auth.service.UserNotificationService;
import org.postgresql.*;

@Repository
public class RequestDAOImpl implements RequestDAO, GenericDAO<Request> {
    private final Logger logger = LoggerFactory.getLogger(RequestDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final UserService userService;
    private final Conf conf;
    private final UserNotificationService userNotifService;


    @Autowired
    public RequestDAOImpl(DaoJdbcUtil daoutil, UserService userService, Conf conf,UserNotificationService userNotifService) {
        this.daoutil = daoutil;
        this.userService=userService;
        this.conf = conf;
        this.userNotifService = userNotifService;
    }

    @Override
    public String getSqlPath() {
        return "requests";
    }

    @Override
    public void sendToExec(String execMail, Long n_req_id, String req_address, String req_type, Date dead_line) {
        MDC.put("action", "sendToExec");
        // Оповещение исполнителя
        //dead_line.toLocaleString()
        int n_cp=44;
        try {
            if (execMail!=null){
                logger.info("sendToExec, send mess to = " + execMail);
                n_cp++;
                Map<String, String> par_send = new HashMap<>();
                n_cp++;
                par_send.put("MSG_CODE","REQ_ASSIGN");
                par_send.put("MSG_LANG","RU");
                n_cp++;
                par_send.put("EMAIL",execMail);
                n_cp++;
                par_send.put("REQ_ID", n_req_id.toString());
                n_cp++;
                par_send.put("REQ_ADDRESS", req_address);
                n_cp++;
                par_send.put("REQ_TYPE",req_type);
                n_cp++;
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                par_send.put("DEAD_LINE",sdf.format(dead_line));
                n_cp++;
                par_send.put("INSTRUCTIONS_URL",conf.getHost() + "/request.html#reqSearch");
                n_cp++;
                //par_send.put("PUSH", "1");
                userNotifService.sendOtherMess(par_send);
                n_cp++;
                logger.info("sendToExec, sended");
            }
            else
                logger.info("sendToExec, Email is null");
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            throw null;
        }
    }

    @Override
    public Class<Request> getEntityType() {
        return Request.class;
    }

    @Override
    public String inserts(Map<String, Object> params, User user) {
        MDC.put("action", "inserts");
        int par_cnt = params.size();
        int n_cp=70;
        Object[] par_val;
        logger.info(" RequestDAO, par_cnt="+par_cnt+", sqlpath="+params.get("sqlpath"));

        int n_id = -1;
        int nn  = 0;
        n_cp++;
        String sqlCode = null;
        n_cp++;
        String userEmail = new String();
        String lang_id;
        String sid;
		String errors=new String();
        Object cur_hist=null;
        Long t_request_id=null;
        String req_address=null;
        String req_type=null;
        Date dead_line=null;
        Long t_flat_id=null;
        n_cp++;
        try {
            for (String key : params.keySet()) {
                if (key.equals("userMail")) {
                    par_cnt -= 1;
                }else if (key.equals("t_language_id")){
                    par_cnt -= 1;
                }else if (key.equals("cur_hist")) {
                    par_cnt -= 1;
                }else if (key.equals("addressId")){
					par_cnt -= 1;
				}else if (key.equals("req_address")) {
                    par_cnt -= 1;
                }else if (key.equals("req_type")) {
                    par_cnt -= 1;
                }
                if (params.get("sqlpath").equals("reject_other_inqs")) {
                    if (key.equals("comp_id")) {
                        par_cnt -= 1;
                    }

                }
            }


            n_cp++;

            Object[] new_params = new Object[par_cnt - 1];
            for (String key : params.keySet()) {
                nn++;
                Object val = params.get(key);
                if (key.equals("sqlpath")){
                    logger.info(" sqlpath " + nn +", key=" + key+" val="+val);
                    sqlCode = val.toString();
                }else if (key.equals("cur_hist")){
                    cur_hist= params.get("cur_hist");
                    logger.info(" cur_hist="+cur_hist.toString());
                }else if (key.equals("userMail")){
                    userEmail=val.toString();
                }else if (key.equals("t_language_id")){
                    lang_id=val.toString();
                    logger.info(" lang_id="+lang_id);
                }else if (key.equals("addressId")){
					}
                else if (key.equals("comp_id")&&params.get("sqlpath").equals("reject_other_inqs")) {

                }
                else {
                    if (key.substring(0,1).equals("d")) {
                        logger.info(" RequestDAO, convert to Date [" + val.toString() + "]");
                        if (val.toString()=="null") {
                            logger.info(" RequestDAO, null Date");
                            val=null;
                        }
                        else {
                            n_cp=140;
                            val = daoutil.StrtoDate(val.toString());
                            if (key.equals("dead_line")){
                                n_cp++;
                                dead_line=(Date) val;
                            }
                        }
                    }

                    n_id++;
                    new_params[n_id] = val;
                    logger.info(" RequestDAO, " + n_id + "," + key +".val=" + val);
                }
            }
            n_cp = 156;

            logger.info(" new_params_size.len="+new_params.length);
            n_cp = 159;
            for (int i =0; i<new_params.length; i++){
                logger.info(" new_params("+ i + ")=" + new_params[i]);
            }
			try {
                n_cp = 163;
                logger.debug("sqlCode={}{}"+sqlCode);

                KeyHolder kh =daoutil.inserts(this,sqlCode, new_params);
                n_cp = 166;
                n_cp++;

                if (sqlCode.equals("insert_req")) {
                    t_request_id=(Long) kh.getKeys().get("status");
                if (params.get("sqlpath").toString().equals("req_after_auth")){
                    return null;
                }
                n_cp = 172;
                    String fract;
                    if (params.get("req_fract").equals("")) {
                        //logger.info("tttttttttttttt = "+params.get("req_fract"));
                        fract = null;
                        //logger.info("qqqqqqqqqqqqqq = "+fract);
                    }
                    else {
                        fract = (String) params.get("req_fract");
                    }
                    par_val = new Object[4];
                    par_val[0] = (Double) params.get("req_building");
                    par_val[1] = Integer.parseInt(params.get("req_flat").toString());
                    par_val[2] = fract;
                    par_val[3] = fract;
                    t_flat_id=daoutil.IdByParams("sprav/flat_id_by_params", par_val);
                    req_address=daoutil.textByID("sprav/address_by_flat", t_flat_id);
                }else {
                    if (sqlCode.equals("insert_cit_req_serv")) {
                        t_request_id=(Long) kh.getKeys().get("status");
                    }
                    else {
                    t_request_id=(Long) kh.getKeys().get("id");
                    }
                    if (params.get("sqlpath").toString().equals("req_after_auth")){
                        return null;
                    }
                    n_cp = 172;
                req_address=daoutil.textByID("sprav/address_by_flat", (Double) params.get("req_flat"));
                }

                if (sqlCode.equals("insert_cit_req_serv")) {
                    String serv_sub = "";
                    String usermail = "";
                    String serv_str = (String) params.get("note");
                    String[] serv_arr = serv_str.split(",");
                    long[] serv_arr_int = new long[serv_arr.length];
                    for (int i = 0; i < serv_arr.length; i++) {
                        if (serv_arr[i] != null && (!serv_arr[i].equals("")) && (!serv_arr[i].equals("0"))) {
                            serv_arr_int[i] = Long.parseLong(serv_arr[i]);
                        }
                    }
                    for (long servs_id : serv_arr_int) {
                        if (servs_id != 0) {
                            if (serv_sub.equals("")) {
                                serv_sub = daoutil.textByID("sprav/req_type_by_subtype", servs_id);
                            }
                            else {
                                serv_sub = serv_sub+ " => " +daoutil.textByID("sprav/req_type_by_subtype", servs_id);
                            }
                        }
                    }
                    String services=daoutil.textByID("sprav/req_type_by_subtype",(Double) params.get("main_serv"));
                    if (!serv_sub.equals("")) {
                        services = services+ " => " + serv_sub;
                    }
                    //logger.info("getSimpleName = "+params.get("req_flat").getClass().getSimpleName());
                    int city_id = daoutil.IdById("sprav/get_city_id_by_flat", params.get("req_flat"));
                    String city_name=daoutil.textByID("sprav/get_city_name", city_id);
                    Double stab_id;
                    Long serv_id;
                    logger.info("getSimpleNameSubtype = "+params.get("req_subtype").getClass().getSimpleName());
                    if (params.get("req_subtype").getClass().getSimpleName().equals("Double")) {
                        stab_id = (Double) params.get("req_subtype");
                        serv_id=Math.round(stab_id);
                        //logger.info("double");
                        //equals("class java.lang.Double")
                    }
                    else {
                        //logger.info("string");
                        serv_id = Long.parseLong(params.get("req_subtype").toString());
                    }

                    List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_serv_id", serv_id, city_id);
                    //logger.info("s_position="+s_res.size());
                    for (int i = 0; i < s_res.size(); i++) {
                        String mailCheck = "";
                        mailCheck = s_res.get(i).toString().replace("{text=","");
                        mailCheck = mailCheck.toString().replace("}","");
                        if (Validator.isValidEmail(mailCheck)) {
                            if (usermail.equals(""))
                                usermail = mailCheck;
                            else
                                usermail = usermail+ ", " +mailCheck;
                        }
                    }
                    if (sqlCode.equals("insert_cit_req_serv")) {
                        Map<String, String> par_send = new HashMap<>();
                        n_cp++;
                        par_send.put("MSG_CODE", "NEW_REQ_TO_COMP");
                        par_send.put("MSG_LANG", "RU");
                        par_send.put("REQ_ID", t_request_id.toString());
                        par_send.put("REQ_TYPE", services);
                        par_send.put("CITY_NAME", city_name);
                        par_send.put("ACCEPT_KSK", conf.getHost() + "/servrequest.html");
                        par_send.put("EMAIL",usermail);

                        userNotifService.sendOtherMessToAll(par_send);
                    }

                }
                else {
                req_type=daoutil.textByID("sprav/req_type_by_subtype", (Double) params.get("req_subtype"));
                }
                logger.info("t_request_id="+t_request_id);
                n_cp++;
                if (cur_hist==null) {
                    logger.info("cur_hist is null");
                }
                else {
                    n_cp = 187;
                    logger.info("cur_hist=" + cur_hist.toString());
                    ReqHistory reqHist = new ReqHistory();
                    reqHist.setT_request_id(t_request_id);
                    ArrayList arr1;
                    Map<String, Object> m_str;
                    arr1 = (ArrayList) cur_hist;
                    reqHist.setSid(0);
                    reqHist.setId((long) 0);
                    Date curdate;

                    int disp_id = daoutil.IdById("sprav/disp_id", user.getUserId().doubleValue());
                    if (disp_id>0) {
                        logger.info("disp_id=" + disp_id);
                        reqHist.setDisp_id(disp_id);
                    }

                    for (int i = 0; i < arr1.size(); i++) {
                        m_str = (Map<String, Object>) arr1.get(i);
                        n_cp = 193;
                        logger.info("===== ReqDAO.insert, i=" + i +"=====");
                        logger.info("t_position_id=" + m_str.get("t_position_id"));
                        n_cp++;
                        reqHist.setT_position_id((int) Math.round((Double) m_str.get("t_position_id")));
                        logger.info("t_req_status_id=" + m_str.get("t_req_status_id"));
                        n_cp++;
                        reqHist.setT_req_status_id((int) Math.round((Double) m_str.get("t_req_status_id")));
                        logger.info("note=" + m_str.get("note"));
                        n_cp++;
                        reqHist.setT_note((String) m_str.get("note"));
                        curdate = new Date();
                        n_cp++;
                        reqHist.setD_history(curdate);
                        logger.info("D_history=" + reqHist.getD_history());
                        n_cp++;
                        reqHist.setExec_Email(daoutil.textByID("sprav/position_email",reqHist.getT_position_id().doubleValue()));
                        n_cp++;
                        sid = reqHist(reqHist, "insert_req_his");
                        logger.info("sid=" + sid);
                        n_cp=211;
                        if (reqHist.getSid()==0){
                            n_cp=213;
                            reqHist.setSid(Integer.parseInt(sid));
                        }
                        // Оповещение исполнителя
                        n_cp=235;
                        logger.info("reqHist.getExec_Email()=" + reqHist.getExec_Email());
                        logger.info("reqHist.getT_request_id()=" + reqHist.getT_request_id());
                        logger.info("req_address=" + req_address);
                        logger.info("req_type=" + req_type);
                        logger.info("dead_line=" + dead_line);
                        sendToExec(reqHist.getExec_Email(), reqHist.getT_request_id(), req_address, req_type, dead_line);
                        n_cp=242;
                        }
                    }
			}catch (Exception e){
                errors=e.toString();
                return errors;
            }

            n_cp=245;
            if (sqlCode.equals("insert_req")) {
                    Map<String, String> par_send = new HashMap<>();
                    n_cp++;
                    par_send.put("MSG_CODE","REQ_CREATE");
                    par_send.put("MSG_LANG","RU");
                    par_send.put("EMAIL",userEmail);
                    n_cp++;
                    par_send.put("REQ_ID",t_request_id.toString());
                    n_cp++;
                    par_send.put("REQ_ADDRESS", req_address);
                    n_cp++;
                    par_send.put("REQ_TYPE",req_type);
                    n_cp++;
                    par_send.put("INSTRUCTIONS_URL",conf.getHost() + "/citizenrequest.html#citReqSearch");
                    n_cp++;
                    userService.sendNotif(par_send);
            }
            else if (sqlCode.equals("reject_other_inqs")) {
                String usermail = "";
                List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", params.get("comp_id"));
                for (int i = 0; i < s_res.size(); i++) {
                    String mailCheck1 = "";
                    mailCheck1 = s_res.get(i).get("text").toString();
                    logger.info("mailCheck1==="+mailCheck1);
                    if (Validator.isValidEmail(mailCheck1)) {
                        if (usermail.equals(""))
                            usermail = mailCheck1;
                        else
                            usermail = usermail+ ", " +mailCheck1;
                    }
                }
                Map<String, String> par_send = new HashMap<>();
                n_cp++;
                par_send.put("MSG_CODE", "SERV_INQ_REJECT");
                par_send.put("MSG_LANG", "RU");
                par_send.put("EMAIL",usermail);
                par_send.put("REASON_NOTE", params.get("reason_note").toString());
                userNotifService.sendOtherMessToAll(par_send);
            }else if(sqlCode.equals("insert_cit_req")){
                String usermail = "";
                List<Map<String, Object>>  kskEmail = null;
                kskEmail = daoutil.queryForMapList( "user/ksk_email_by_flat_id",params.get("req_flat"), user.getUserId());
                for (int i = 0; i < kskEmail.size(); i++) {
                    String mailCheck1 = "";
                    mailCheck1 = kskEmail.get(i).get("usermail1").toString();
                    logger.info("mailCheck1==="+mailCheck1);
                    if (Validator.isValidEmail(mailCheck1)) {
                        if (usermail.equals(""))
                            usermail = mailCheck1;
                        else
                            usermail = usermail+ ", " +mailCheck1;
                    }
                }
                Map<String, String> par_send = new HashMap<>();
                String services=daoutil.textByID("sprav/req_type_by_subtype",(Double) params.get("req_subtype"));
                n_cp++;
                par_send.put("MSG_CODE", "NEW_REQ_TO_KSK");
                par_send.put("MSG_LANG", "RU");
                par_send.put("REQ_ID", t_request_id.toString());
                par_send.put("REQ_TYPE", services);
                par_send.put("ACCEPT_KSK", conf.getHost() + "/request.html");
                par_send.put("EMAIL",usermail);
                //par_send.put("PUSH", "1");
                userNotifService.sendOtherMessToAll(par_send);
            }
            return t_request_id.toString();
        }
        catch (Exception e) {
            //logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
        }
        logger.info("===== inserts end =====");
        throw null;
    }

    public String execFunction(Map<String, Object> params, User user) {
        int par_cnt = params.size();
        int n_cp=70;
        logger.info(" RequestDAO, par_cnt="+par_cnt+", sqlpath="+params.get("sqlpath"));

        int n_id = -1;
        int nn  = 0;
        n_cp++;
        String sqlCode = null;
        n_cp++;
        String userEmail = new String();
        String lang_id;
        String sid;
        String errors=new String();
        Object cur_hist=null;
        Long t_request_id=null;
        String req_address=null;
        String req_type=null;
        Date dead_line=null;
        n_cp++;
        try {
            for (String key : params.keySet()) {
                if (key.equals("userMail")) {
                    par_cnt -= 1;
                }else if (key.equals("t_language_id")){
                    par_cnt -= 1;
                }else if (key.equals("cur_hist")) {
                    par_cnt -= 1;
                }else if (key.equals("addressId")){
                    par_cnt -= 1;
                }else if (key.equals("req_address")) {
                    par_cnt -= 1;
                }else if (key.equals("req_type")) {
                    par_cnt -= 1;
                }
            }
            n_cp++;

            Object[] new_params = new Object[par_cnt - 1];
            for (String key : params.keySet()) {
                nn++;
                Object val = params.get(key);
                if (key.equals("sqlpath")){
                    logger.info(" sqlpath " + nn +", key=" + key+" val="+val);
                    sqlCode = val.toString();
                }else if (key.equals("cur_hist")){
                    cur_hist= params.get("cur_hist");
                    logger.info(" cur_hist="+cur_hist.toString());
                }else if (key.equals("userMail")){
                    userEmail=val.toString();
                }else if (key.equals("t_language_id")){
                    lang_id=val.toString();
                    logger.info(" lang_id="+lang_id);
                }else if (key.equals("addressId")){
                }
                else {
                    if (key.substring(0,1).equals("d")) {
                        logger.info(" RequestDAO, convert to Date [" + val.toString() + "]");
                        if (val.toString()=="null") {
                            logger.info(" RequestDAO, null Date");
                            val=null;
                        }
                        else {
                            n_cp=140;
                            val = daoutil.StrtoDate(val.toString());
                            if (key.equals("dead_line")){
                                n_cp++;
                                dead_line=(Date) val;
                            }
                        }
                    }

                    n_id++;
                    new_params[n_id] = val;
                    logger.info(" RequestDAO, " + n_id + "," + key +".val=" + val);
                }
            }
            n_cp = 156;

            logger.info(" new_params_size.len="+new_params.length);
            n_cp = 159;
            for (int i =0; i<new_params.length; i++){
                logger.info(" new_params("+ i + ")=" + new_params[i]);
            }
            Integer kh;
            try {
                n_cp = 163;
                logger.debug("sqlCode={}{}"+sqlCode);

                 kh= daoutil.execStoredFunc2(sqlCode,4,new_params);

                if (kh == 0) {
                      String usermail = "";
                  if (params.get("sqlpath").equals("f_serv_req_upds")) {
                      if (params.get("status_serv_req").toString().equals("3.0")) {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_sel_comp_mail_by_serv_req_id", params.get("id_serv_req"));
                          //logger.info("s_position="+s_res.size());
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck = "";
                              mailCheck = s_res.get(i).toString().replace("{text=","");
                              mailCheck = mailCheck.toString().replace("}","");
                              if (Validator.isValidEmail(mailCheck)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck;
                                  else
                                      usermail = usermail+ ", " +mailCheck;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "REQ_TO_SELECTED_COMP");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("REQ_ID",params.get("req_id").toString());
                          par_send.put("ACCEPT_KSK", conf.getHost() + "/servrequest.html");
                          par_send.put("EMAIL",usermail);

                          userNotifService.sendOtherMessToAll(par_send);

                      }
                      if (params.get("status_serv_req").toString().equals("7.0")) {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_req_id_and_status", params.get("req_id"),params.get("status_serv_req"));
                          //logger.info("s_position="+s_res.size());
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck = "";
                              mailCheck = s_res.get(i).toString().replace("{text=","");
                              mailCheck = mailCheck.toString().replace("}","");
                              if (Validator.isValidEmail(mailCheck)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck;
                                  else
                                      usermail = usermail+ ", " +mailCheck;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "REQ_SERV_REOPEN");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("REQ_ID",params.get("req_id").toString());
                          par_send.put("ACCEPT_KSK", conf.getHost() + "/servrequest.html");
                          par_send.put("EMAIL",usermail);

                          userNotifService.sendOtherMessToAll(par_send);

                      }
                      if (params.get("status_serv_req").toString().equals("6.0")) {
                          List<Map<String, Object>> s_res1 = daoutil.queryForMapList("sprav/get_comp_mail_by_req_id_and_status", params.get("req_id"),params.get("status_serv_req"));
                          if (s_res1.size() == 0) {
                              List<Map<String, Object>> s_res2 = daoutil.queryForMapList("sprav/get_serv_city_by_req_id", params.get("req_id"));
                              List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_serv_id", s_res2.get(0).get("t_services_id"), s_res2.get(0).get("t_city_id"));
                              //logger.info("s_position="+s_res.size());
                              for (int i = 0; i < s_res.size(); i++) {
                                  String mailCheck = "";
                                  mailCheck = s_res.get(i).toString().replace("{text=", "");
                                  mailCheck = mailCheck.toString().replace("}", "");
                                  if (Validator.isValidEmail(mailCheck)) {
                                      if (usermail.equals(""))
                                          usermail = mailCheck;
                                      else
                                          usermail = usermail + ", " + mailCheck;
                                  }
                              }
                              Map<String, String> par_send = new HashMap<>();
                              n_cp++;
                              par_send.put("MSG_CODE", "CLOSE_REQ");
                              par_send.put("MSG_LANG", "RU");
                              par_send.put("T_REQUEST_ID", params.get("req_id").toString());
                              par_send.put("ACCEPT_KSK", conf.getHost() + "/servrequest.html");
                              par_send.put("EMAIL", usermail);

                              userNotifService.sendOtherMessToAll(par_send);

                          }
                      }
                      if (params.get("status_serv_req").toString().equals("4.0")) {
                          List<Map<String, Object>> s_res1 = daoutil.queryForMapList("sprav/get_usermail_by_req_id", params.get("req_id"));
                          if (s_res1.size() != 0) {
                              String mailCheck = "";
                              for (int i = 0; i < s_res1.size(); i++) {
                                  mailCheck = s_res1.get(i).get("usermail1").toString();
                                  if (Validator.isValidEmail(mailCheck)) {
                                      if (usermail.equals(""))
                                          usermail = mailCheck;
                                  }
                              }
                              Map<String, String> par_send = new HashMap<>();
                              n_cp++;
                              par_send.put("MSG_CODE", "REQ_EXECUTED");
                              par_send.put("MSG_LANG", "RU");
                              par_send.put("REQ_ID",  Long.toString(Math.round((Double) params.get("req_id"))));
                              par_send.put("INSTRUCTIONS_URL", conf.getHost() + "/citizenrequest.html#citReqSearch");
                              par_send.put("EMAIL", usermail);

                              userNotifService.sendOtherMess(par_send);

                          }
                      } if (params.get("status_serv_req").toString().equals("8.0")) {
                          List<Map<String, Object>> s_res1 = daoutil.queryForMapList("sprav/get_usermail_by_req_id", params.get("req_id"));
                          if (s_res1.size() != 0) {
                              String mailCheck = "";
                              for (int i = 0; i < s_res1.size(); i++) {
                                  mailCheck = s_res1.get(i).get("usermail1").toString();
                                  if (Validator.isValidEmail(mailCheck)) {
                                  }
                              }
                              Map<String, String> par_send = new HashMap<>();
                              n_cp++;
                              par_send.put("MSG_CODE", "REQ_SERV_REJECT");
                              par_send.put("MSG_LANG", "RU");
                              par_send.put("REQ_ID", Long.toString(Math.round((Double) params.get("req_id"))));
                              par_send.put("REASON", params.get("s_note").toString());
                              par_send.put("INSTRUCTIONS_URL", conf.getHost() + "/citizenrequest.html#citReqSearch");
                              par_send.put("EMAIL", mailCheck);

                              userNotifService.sendOtherMess(par_send);

                          }

                          //Long req_id = Long.parseLong(params.get("req_id").toString());
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_req_id", params.get("req_id"));
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck1 = "";
                              mailCheck1 = s_res.get(i).get("text").toString();
                              logger.info("mailCheck1==="+mailCheck1);
                              if (Validator.isValidEmail(mailCheck1)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck1;
                                  else
                                      usermail = usermail+ ", " +mailCheck1;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "REQ_REJECT_SELECTED_COMP");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("REQ_ID",params.get("req_id").toString());
                          par_send.put("ACCEPT_KSK", conf.getHost() + "/servrequest.html");
                          par_send.put("EMAIL",usermail);
                          userNotifService.sendOtherMessToAll(par_send);

                      }
                    }else if ((params.get("sqlpath").equals("f_edit_serv_function") && (!params.get("quest_arr").toString().isEmpty())) ||
                                (params.get("sqlpath").equals("f_reg_supplier_service2") && (!params.get("addquest").toString().isEmpty()))) {
                        Map<String, Object> params2=new HashMap<String, Object>();

                        params2.put("comp_id", params.get("compid"));
                       /* params2.put("city_id", params.get("city_arr"));*/
                        params2.put("sqlpath", "sprav/get_comp_moderatormail");
                        /*params2.put("code", params.get("sqlpath"));*/
                        params2.put("lang_id", params.get("lang_id"));
                        List<Map<String, Object>> s_res1 = daoutil.queryForMapList(params2);
                        if (s_res1.size() != 0) {
                            String mailCheck = "", mailCheck1 = "", compName="";
                            compName=s_res1.get(0).get("comp_name").toString();
                            for (int i = 0; i < s_res1.size(); i++) {
                                mailCheck = s_res1.get(i).get("email").toString();
                                if (Validator.isValidEmail(mailCheck)) {
                                    mailCheck1 = mailCheck1+ ", " +mailCheck;
                                }
                            }
                            Map<String, String> par_send = new HashMap<>();
                            n_cp++;
                            par_send.put("MSG_CODE", "REQ_SERV_EDIT");
                            par_send.put("MSG_LANG", "RU");
                            par_send.put("COMP_NAME",compName);
                            par_send.put("INSTRUCTIONS_URL", conf.getHost() + "/servquestinquire.html");
                            par_send.put("EMAIL", mailCheck1);

                            userNotifService.sendOtherMessToAll(par_send);
                        }

                    }else if (params.get("sqlpath").equals("f_reg_supplier_service")) {
                        Map<String, Object> params2=new HashMap<String, Object>();
                        params2.put("sqlpath", "sprav/get_all_comp_moderatormail");
                        List<Map<String, Object>> s_res1 = daoutil.queryForMapList(params2);
                        if (s_res1.size() != 0) {
                            String mailCheck = "", mailCheck1 = "";
                            String[] compName = params.get("cinfo").toString().split("\\,", -1);
                            for (int i = 0; i < s_res1.size(); i++) {
                                mailCheck = s_res1.get(i).get("email").toString();
                                if (Validator.isValidEmail(mailCheck)) {
                                    mailCheck1 = mailCheck1+ ", " +mailCheck;
                      }
                            }
                            logger.info("comp_name="+compName[6]);
                            logger.info("comp_bin="+compName[5]);
                            Map<String, String> par_send = new HashMap<>();
                            n_cp++;
                            par_send.put("MSG_CODE", "REG_SERV");
                            par_send.put("MSG_LANG", "RU");
                            par_send.put("COMP_NAME",compName[6]);
                            par_send.put("COMP_BIN",compName[5]);
                            par_send.put("INSTRUCTIONS_URL", conf.getHost() + "/servquestinquire.html");
                            par_send.put("EMAIL", mailCheck1);

                            userNotifService.sendOtherMessToAll(par_send);
                        }

                  }
                  else if (params.get("sqlpath").equals("f_comp_act_deact")){
                      if (params.get("status").toString().equals("1.0")) {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", params.get("comp_id"));
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck1 = "";
                              mailCheck1 = s_res.get(i).get("text").toString();
                              logger.info("mailCheck1==="+mailCheck1);
                              if (Validator.isValidEmail(mailCheck1)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck1;
                                  else
                                      usermail = usermail+ ", " +mailCheck1;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "COMP_ACTIVATE");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("EMAIL",usermail);
                          userNotifService.sendOtherMessToAll(par_send);
                      }
                      else if (params.get("status").toString().equals("0.0")) {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", params.get("comp_id"));
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck1 = "";
                              mailCheck1 = s_res.get(i).get("text").toString();
                              logger.info("mailCheck1==="+mailCheck1);
                              if (Validator.isValidEmail(mailCheck1)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck1;
                                  else
                                      usermail = usermail+ ", " +mailCheck1;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "COMP_DEACTIVATE");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("EMAIL",usermail);
                          userNotifService.sendOtherMessToAll(par_send);
                      }
                  }
                  else if (params.get("sqlpath").equals("f_ins_servs")) {
                      if (params.get("serv_id").toString().equals("-1.0")) {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_all_comp_mail",params.get("comp_id"));
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck1 = "";
                              mailCheck1 = s_res.get(i).get("text").toString();
                              logger.info("mailCheck1==="+mailCheck1);
                              if (Validator.isValidEmail(mailCheck1)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck1;
                                  else
                                      usermail = usermail+ ", " +mailCheck1;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          par_send.put("MSG_CODE", "ADD_NEW_SERV");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("EMAIL",usermail);
                          par_send.put("SERVS",params.get("servs").toString().replaceAll(",","=>"));
                          par_send.put("INSTRUCTIONS_URL",conf.getHost() + "/suppservedit.html#suppserveditView");
                          userNotifService.sendOtherMessToAll(par_send);
                          if (!params.get("comp_id").toString().equals("0.0")) {
                              usermail = "";
                              s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", params.get("comp_id"));
                              for (int i = 0; i < s_res.size(); i++) {
                                  String mailCheck1 = "";
                                  mailCheck1 = s_res.get(i).get("text").toString();
                                  logger.info("mailCheck1==="+mailCheck1);
                                  if (Validator.isValidEmail(mailCheck1)) {
                                      if (usermail.equals(""))
                                          usermail = mailCheck1;
                                      else
                                          usermail = usermail+ ", " +mailCheck1;
                                  }
                              }
                              par_send = new HashMap<>();
                              n_cp++;
                              par_send.put("MSG_CODE", "ADD_NEW_SERV_COMP");
                              par_send.put("MSG_LANG", "RU");
                              par_send.put("EMAIL",usermail);
                              par_send.put("SERVS",params.get("servs").toString().replaceAll(",","=>"));
                              userNotifService.sendOtherMessToAll(par_send);
                          }
                      }
                      else {
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_all_comp_mail_by_serv",params.get("serv_id"),params.get("comp_id"));
                          for (int i = 0; i < s_res.size(); i++) {
                              String mailCheck1 = "";
                              mailCheck1 = s_res.get(i).get("text").toString();
                              logger.info("mailCheck1==="+mailCheck1);
                              if (Validator.isValidEmail(mailCheck1)) {
                                  if (usermail.equals(""))
                                      usermail = mailCheck1;
                                  else
                                      usermail = usermail+ ", " +mailCheck1;
                              }
                          }
                          Map<String, String> par_send = new HashMap<>();
                          n_cp++;
                          String services=daoutil.textByID("sprav/req_type_by_subtype",(Double) params.get("serv_id"));
                          par_send.put("MSG_CODE", "ADD_NEW_SERV");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("EMAIL",usermail);
                          par_send.put("SERVS",services+"=>"+params.get("servs").toString().replaceAll(",","=>"));
                          par_send.put("INSTRUCTIONS_URL",conf.getHost() + "/suppservedit.html#suppserveditView");
                          userNotifService.sendOtherMessToAll(par_send);
                          if (!params.get("comp_id").toString().equals("0.0")) {
                              s_res = daoutil.queryForMapList("sprav/get_comp_mail_by_comp_id", params.get("comp_id"));
                              for (int i = 0; i < s_res.size(); i++) {
                                  String mailCheck1 = "";
                                  mailCheck1 = s_res.get(i).get("text").toString();
                                  logger.info("mailCheck1==="+mailCheck1);
                                  if (Validator.isValidEmail(mailCheck1)) {
                                      if (usermail.equals(""))
                                          usermail = mailCheck1;
                                      else
                                          usermail = usermail+ ", " +mailCheck1;
                                  }
                              }
                              par_send = new HashMap<>();
                              n_cp++;
                              par_send.put("MSG_CODE", "ADD_NEW_SERV_COMP");
                              par_send.put("MSG_LANG", "RU");
                              par_send.put("EMAIL",usermail);
                              par_send.put("SERVS",services+"=>"+params.get("servs").toString().replaceAll(",","=>"));
                              userNotifService.sendOtherMessToAll(par_send);
                          }
                      }
                  }
                  else if (params.get("sqlpath").equals("f_upd_servs")&&params.get("type").equals("SERVICE")) {
                      List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_all_comp_mail_by_serv",params.get("serv_id"),0);
                      for (int i = 0; i < s_res.size(); i++) {
                          String mailCheck1 = "";
                          mailCheck1 = s_res.get(i).get("text").toString();
                          logger.info("mailCheck1==="+mailCheck1);
                          if (Validator.isValidEmail(mailCheck1)) {
                              if (usermail.equals(""))
                                  usermail = mailCheck1;
                              else
                                  usermail = usermail+ ", " +mailCheck1;
                          }
                      }
                      Map<String, String> par_send = new HashMap<>();
                      n_cp++;
                      String services=daoutil.textByID("sprav/req_type_by_subtype",(Double) params.get("serv_id"));
                      if (params.get("status").toString().equals("1.0")) {
                          par_send.put("MSG_CODE", "SERV_ACTIVATE");
                      }
                      else {
                          par_send.put("MSG_CODE", "SERV_DEACTIVATE");
                      }
                      par_send.put("MSG_LANG", "RU");
                      par_send.put("EMAIL",usermail);
                      par_send.put("SERVS",services);
                      userNotifService.sendOtherMessToAll(par_send);
                  }
                }

                n_cp++;

            }catch (Exception e){
                errors=e.toString();
                return errors;
            }

            return kh.toString();
        }
        catch (Exception e) {
            //logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
        }
        logger.info("===== inserts end =====");
        throw null;
    }

    public String execFunction3(Map<String, Object> params, User user) {




        int par_cnt = params.size();
        int n_cp=70;
        logger.info(" RequestDAO, par_cnt="+par_cnt+", sqlpath="+params.get("sqlpath"));

        int n_id = -1;
        int nn  = 0;
        n_cp++;
        String sqlCode = null;
        n_cp++;
        String userEmail = new String();
        String lang_id;
        String sid;
        String errors=new String();
        Object cur_hist=null;
        Long t_request_id=null;
        String req_address=null;
        String req_type=null;
        Date dead_line=null;
        Class PgArray;
        n_cp++;
        try {
            for (String key : params.keySet()) {
                if (key.equals("userMail")) {
                    par_cnt -= 1;
                }else if (key.equals("t_language_id")){
                    par_cnt -= 1;
                }else if (key.equals("cur_hist")) {
                    par_cnt -= 1;
                }else if (key.equals("addressId")){
                    par_cnt -= 1;
                }else if (key.equals("req_address")) {
                    par_cnt -= 1;
                }else if (key.equals("req_type")) {
                    par_cnt -= 1;
                }
            }
            n_cp++;

            Object[] new_params = new Object[par_cnt - 1];
            for (String key : params.keySet()) {
                nn++;





                Object val = params.get(key);
                if (key.equals("sqlpath")){
                    logger.info(" sqlpath " + nn +", key=" + key+" val="+val);

                    sqlCode = val.toString();
                }else if (key.equals("cur_hist")){
                    cur_hist= params.get("cur_hist");
                    logger.info(" cur_hist="+cur_hist.toString());
                }else if (key.equals("userMail")){
                    userEmail=val.toString();
                }else if (key.equals("t_language_id")){
                    lang_id=val.toString();
                    logger.info(" lang_id="+lang_id);
                }else if (key.equals("addressId")){
                }
                else {
                    if (key.substring(0,1).equals("d")) {
                        logger.info(" RequestDAO, convert to Date [" + val.toString() + "]");
                        if (val.toString()=="null") {
                            logger.info(" RequestDAO, null Date");
                            val=null;
                        }
                        else {
                            n_cp=140;
                            val = daoutil.StrtoDate(val.toString());
                            if (key.equals("dead_line")){
                                n_cp++;
                                dead_line=(Date) val;
                            }
                        }
                    }

                    n_id++;

                    new_params[n_id] = val;
                    logger.info(" RequestDAO, " + n_id + "," + key +".val=" + val);
                }
            }
            n_cp = 156;

            logger.info(" new_params_size.len="+new_params.length);
            n_cp = 159;
            for (int i =0; i<new_params.length; i++){
                logger.info(" new_params("+ i + ")=" + new_params[i]);
            }
            java.sql.Array kh;
            try {
                n_cp = 163;
                logger.debug("sqlCode={}{}"+sqlCode);
                kh=daoutil.execStoredFunc3(sqlCode,2003,new_params);
                String strData[] = (String[]) kh.getArray();
                n_cp++;

            }catch (Exception e){
                errors=e.toString();
                return errors;
            }

            return kh.toString();
        }
        catch (Exception e) {
            //logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
        }
        logger.info("===== inserts end =====");
        throw null;
    }

    @Override
    public String updates(Map<Integer, Object> params, Boolean many) {
        MDC.put("action","RequestDAO.updates");
        logger.info("===== updates beg ===== line 274");
        int n_cp=130;
        try {
            int par_cnt = params.size();
            //logger.info("RequestDAO.updates, par_cnt1="+par_cnt);

            int n_id = -1;
            String sqlCode = null;
            Object[] new_params = new Object[par_cnt - 1];

            n_cp=139;
            logger.info("params=" + params.toString());
            for (Integer key : params.keySet()) {
                logger.info("key=" + key);

                n_cp=144;
                Object val = params.get(key);
                n_cp=146;
                if (key.equals(0)){
                    n_cp=148;
                    sqlCode = val.toString();
                    logger.info("sqlCode=" + sqlCode);
                }
                else {
                    n_id++;
                    n_cp=154;
                    new_params[n_id] = val;
                    n_cp=156;
                   // logger.info("val_" + n_id + "=[" + key +"].val=" + val + ", type=" + val.getClass());
                    //logger.info("[" + key +"].type=" + val.getClass());
                }
            }
            n_cp=160;
            if (many) {
                logger.info("many=true");
                n_cp=163;
                daoutil.updates(this,sqlCode, new_params);
                return "1";
            }
            else {
                n_cp=168;
                KeyHolder kh =daoutil.updates(this,sqlCode, new_params);
                n_cp=170;
                return kh.getKeys().get("id").toString();
            }
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
        }
        logger.info("===== updates end =====");
        throw null;
    }

    @Override
    public String reqHist(ReqHistory reqHist, String sqlPath) {
        MDC.put("action", "reqHist");
        Object[] values = null;
        logger.info("sqlPath= " + sqlPath);
        Map<Integer, Object> par_req = new TreeMap<Integer, Object>();
        Integer nk = 0;
        int n_cp = 0;
        int cur_id = 0;
        try {
            Date dt = new Date();

            if (sqlPath.equals("insert_req_his")) {
                n_cp = 264;
                if (reqHist.getId()!=0) {
                    par_req.put(nk, "update_req_his_del");
                    nk++;
                    par_req.put(nk, 0);
                    nk++;
                    par_req.put(nk, reqHist.getId());
                    //   Убираем признак удален
                    n_cp = 272;
                    return updates(par_req, true);
                }
                else {
                    n_cp = 276;
                    int cur_sid = reqHist.getSid();
                    logger.info("cur_sid= " + cur_sid);
                    // Если это первое назначение, создаем запись для диспетчера

                    if (cur_sid == 0) {
                        n_cp = 4;
                        values = new Object[]{
                                dt,
                                reqHist.getT_request_id(),
                                reqHist.getDisp_id(),
                                2,
                                "",
                                null
                        };
                        logger.info("values1= " + values.toString());
                        n_cp = 5;
                        KeyHolder kh = daoutil.inserts(this, sqlPath, values);
                        n_cp = 6;
                        logger.info("kh=" + kh.toString());
                        cur_sid = Integer.valueOf(String.valueOf(kh.getKeys().get("id")));
                        n_cp = 7;
                        reqHist.setSid(cur_sid);
                        logger.info("sid=" + cur_sid);
                    }
                    // Создаем запись для исполнителя
                    n_cp = 240;


                    values = new Object[]{
                            dt,
                            reqHist.getT_request_id(),
                            reqHist.getT_position_id(),
                            reqHist.getT_req_status_id(),
                            reqHist.getT_note(),
                            reqHist.getSid()
                    };
                    logger.info(" values2= " + reqHist.getD_history() +"," + reqHist.getT_request_id() + "," + reqHist.getT_position_id()+ ","+
                            reqHist.getT_req_status_id() + ","+ reqHist.getT_note()+","+reqHist.getSid());
                    n_cp = 250;
                    KeyHolder kh = daoutil.inserts(this, sqlPath, values);
                    MDC.put("action", "reqHist");
                    n_cp = 252;
                    cur_id = Integer.valueOf(String.valueOf(kh.getKeys().get("id")));
                    n_cp = 254;
                    return String.valueOf(cur_id);
                }
            }
            else if (sqlPath.equals("exec_req_his")){
                n_cp = 259;
                values = new Object[]{
                        dt,
                        reqHist.getT_request_id(),
                        reqHist.getT_position_id(),
                        reqHist.getT_req_status_id(),
                        reqHist.getT_note(),
                        reqHist.getSid(),
                        reqHist.getUser_id()
                };
                n_cp = 269;
                KeyHolder kh = daoutil.inserts(this, "insert_req_his_by_other_user", values);
                n_cp = 271;
                cur_id = Integer.valueOf(String.valueOf(kh.getKeys().get("id")));
                n_cp = 273;
                return String.valueOf(cur_id);
            }
            else if (sqlPath.equals("insert_req_his_by_user")){
                n_cp = 283;
                values = new Object[]{
                        reqHist.getT_request_id(),
                        reqHist.getT_position_id(),
                        reqHist.getT_req_status_id(),
                        reqHist.getT_note(),
                        reqHist.getUser_id()
                };
                //t_request_id, t_position_id, t_req_status_id, note, t_user_id
                n_cp = 292;
                KeyHolder kh =daoutil.inserts(this, sqlPath, values);
                n_cp = 294;
                cur_id = Integer.valueOf(String.valueOf(kh.getKeys().get("id")));
                return String.valueOf(cur_id);
            }
            else if (sqlPath.equals("reject_req")){
                n_cp = 298;
                values = new Object[]{
                        reqHist.getT_request_id(),
                        reqHist.getT_position_id(),
                        reqHist.getT_req_status_id(),
                        reqHist.getT_note()
                };
                //t_request_id, t_position_id, t_req_status_id, note

                logger.info("getT_request_id="+reqHist.getT_request_id());
                logger.info("getT_position_id="+reqHist.getT_position_id());
                logger.info("getT_req_status_id="+reqHist.getT_req_status_id());
                logger.info("getT_note="+reqHist.getT_note());

                n_cp++;
                KeyHolder kh =daoutil.inserts(this, "insert_req_his_rej", values);
                n_cp++;
                cur_id = Integer.valueOf(String.valueOf(kh.getKeys().get("id")));
                n_cp++;
                return String.valueOf(cur_id);
            }
        }
        catch (Exception e) {
            logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
        }
       n_cp = 271;
       logger.error("n_cp = "+ n_cp + ", Unknown path, sqlPath=[" + sqlPath+"]");
       throw null;
    }
}