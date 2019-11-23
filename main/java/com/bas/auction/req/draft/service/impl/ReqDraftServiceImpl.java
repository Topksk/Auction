package com.bas.auction.req.draft.service.impl;

import com.bas.auction.auth.dao.OneTimeCodeDAOEmail;
import com.bas.auction.auth.dto.OneTimeCodeForEmail;
import com.bas.auction.auth.dto.User;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.req.dao.RequestDAO;
import com.bas.auction.req.draft.service.ReqDraftService;
import com.bas.auction.req.dto.ReqHistory;
import org.apache.commons.codec.binary.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.auth.service.UserNotificationService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.Conf;

import java.util.*;

@Service
public class ReqDraftServiceImpl implements ReqDraftService {
    private final Logger logger = LoggerFactory.getLogger(ReqDraftServiceImpl.class);
    private final RequestDAO reqDAO;
    private final UserService userService;
    private final DaoJdbcUtil daoutil;
    private final Conf conf;
    private final UserNotificationService userNotifService;
    private final OneTimeCodeDAOEmail oneTimeCodeDAOEmail;
    private final DocFileDAO docFileDAO;

    @Autowired
    public ReqDraftServiceImpl(RequestDAO reqDAO, DaoJdbcUtil daoutil, UserService userService, Conf conf, UserNotificationService userNotifService, OneTimeCodeDAOEmail oneTimeCodeDAOEmail, DocFileDAO docFileDAO) {
            this.reqDAO = reqDAO;
            this.daoutil = daoutil;
            this.userService=userService;
            this.conf = conf;
            this.userNotifService = userNotifService;
            this.oneTimeCodeDAOEmail = oneTimeCodeDAOEmail;
            this.docFileDAO = docFileDAO;
    }

    @SpringTransactional
    public String creates(Map<String, Object> params, User user) {
        MDC.put("action", "creates");
        logger.debug("Reqdreaf,params="+params.toString());
        String res, sqlCode;
        int n_cp=44;
        try {
            
            sqlCode=(String) params.get("sqlpath");
            n_cp++;
            Object[] par_val;
            if (sqlCode.equals("ins_employees_ksk")){
                String s_login=(String) params.get("login");
                String s_position = "";
                Integer s_position_ch;
                Integer n_count = 0;
                String s_pos_code;
                if (s_login!=null) {
                    s_login = s_login.substring(s_login.length() - 3, s_login.length());
                    logger.info("position_email, s_login=" + s_login);
                    if (!s_login.equals("@kz")) {
                        String pos_types = (String) params.get("position_types");
                        if (pos_types!=null && !pos_types.isEmpty()){

                            String[] values = pos_types.replaceAll("^[,\\s]+", "").split("[,\\s]+");

                            for(int j = 0; j < values.length; j++) {
                                par_val = new Object[3];
                                par_val[0] = (String) params.get("login");
                                par_val[1] = (Double) params.get("kskId");
                                par_val[2] = values[j];
                                s_position_ch = 0;
                                s_position_ch = daoutil.IdById("sprav/sel_pos_code_ch", par_val);
                                //logger.info("s_position_ch="+s_position_ch);
                                if (s_position_ch == 0) {
                                    logger.info("n_count="+n_count);
                                    if (n_count == 0) {
                                        s_position += daoutil.textByID("sprav/sel_descr_pos_types", values[j]);
                                        n_count = n_count + 1;
                                    }
                                    else {
                                        s_position += ", ";
                                        s_position += daoutil.textByID("sprav/sel_descr_pos_types", values[j]);
                                        n_count = n_count + 1;
                                    }
                                    /*if (j != values.length - 1)
                                        s_position += ", ";
                                    else
                                        s_position += "";*/
                                }
                            }
                            logger.info("s_position="+s_position);

                        }

                        String empMail = (String) params.get("login");
                        Double n_ksk_id = (Double) params.get("kskId");

                        Map<String, String> par_send = new HashMap<>();
                        par_send.put("MSG_CODE", "CREATE_NEW_EMP");
                        par_send.put("MSG_LANG", "RU");
                        //par_send.put("ACCEPT_KSK", conf.getHost() + "/kskProfile.html#kskProfile?inquiry");
                        par_send.put("EMAIL", empMail);
                        par_send.put("POS_DESCR", s_position);
                        par_send.put("KSK_DESCR", daoutil.textByID("sprav/ksk_inf", n_ksk_id, "desc_ru"));
                        par_send.put("LOGIN", empMail);
                        userNotifService.sendOtherMess(par_send);
                    }

                }
            }
            else if (sqlCode.equals("change_disp_addr_reqs")){
                String pos_code = (String) params.get("codetxt");
                Double n_pos_id = (Double) params.get("position");
                Double n_pos_id_new = (Double) params.get("newPos");
                String userEmail="";
                if (n_pos_id_new != 0.0) {
                    userEmail = daoutil.textByID("sprav/get_mail_by_pos_id", n_pos_id_new);
                }
                else {
                    par_val = new Object[2];
                    par_val[0] = n_pos_id;
                    par_val[1] = (Double) params.get("kskId");
                    List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_mail_by_ksk_id_not_in_old_disp", par_val);
                    //logger.info("s_position="+s_res.size());
                    for (int i = 0; i < s_res.size(); i++) {
                        //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                        if (i != s_res.size() - 1)
                            userEmail += s_res.get(i)+",";
                        else
                            userEmail += s_res.get(i);



                    }
                }

                userEmail = userEmail.replace("{text=","");
                userEmail = userEmail.replace("}","");
                Map<String, String> par_send = new HashMap<>();
                Map<String, Object> m_str;

                if (pos_code.equals("ds")) {
                    par_send.put("MSG_CODE", "CIT_INQ_NEW");
                    par_send.put("MSG_LANG", "RU");
                    par_send.put("ACCEPT_KSK", conf.getHost() + "/kskProfile.html#kskProfile?inquiry");
                    par_send.put("EMAIL", userEmail);
                    par_val = new Object[3];
                    par_val[0] = 1;
                    par_val[1] = (Double) params.get("kskId");
                    par_val[2] = n_pos_id;
                    //logger.info("n_pos_id1231="+n_pos_id);
                    //logger.info("dasdasdasd="+(Double) params.get("kskId"));
                    n_cp++;
                    List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/inquiry_search_by_disp", par_val);
                    //logger.info("s_position="+s_res.size());
                    for (int i = 0; i < s_res.size(); i++) {
                        //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                        m_str = s_res.get(i);
                        par_send.put("REG_ADDRESS", (String) m_str.get("address"));
                        par_send.put("REG_FIO", (String) m_str.get("fio_cit"));
                        userNotifService.sendOtherMessToAll(par_send);

                    }

                    // Список не выполненных заявок
                    par_send.put("MSG_CODE", "REQ_NOT_EXEC");
                    par_send.put("REQ_URL", conf.getHost() + "/request.html#reqSearch");
                    par_val = new Object[1];
                    par_val[0] = n_pos_id_new;
                    n_cp++;
                    s_res=daoutil.queryForMapList("sprav/reqs_list_by_disp", par_val);
                    n_cp=144;
                    String s_str="";
                    for(int i= 0 ; i < s_res.size(); i++) {
                        n_cp=146;
                        m_str = s_res.get(i);
                        par_send.put("REQ_ADDRESS", (String) m_str.get("req_addr"));
                        par_send.put("REQ_ID", m_str.get("id").toString());
                        par_send.put("NOTE", (String) m_str.get("note"));
                        par_send.put("STATUS_REQ", (String) m_str.get("status_req"));
                        //Thread.sleep(5000);
                        userNotifService.sendOtherMessToAll(par_send);
                    }
                }
                else {
                    String userEmail_ch = userEmail.substring(userEmail.length() - 3, userEmail.length());
                    if (!userEmail_ch.equals("@kz")){
                        par_send.put("MSG_CODE", "REQ_NOT_EXEC");
                        par_send.put("MSG_LANG", "RU");
                        par_send.put("REQ_URL", conf.getHost() + "/request.html#reqSearch");
                        par_send.put("EMAIL", userEmail);
                        par_val = new Object[1];
                        par_val[0] = n_pos_id_new;
                        n_cp++;
                        List<Map<String, Object>> s_res=daoutil.queryForMapList("sprav/reqs_list_by_exec", par_val);
                        n_cp=144;
                        String s_str="";
                        for(int i= 0 ; i < s_res.size(); i++) {
                            n_cp=146;
                            m_str = s_res.get(i);
                            par_send.put("REQ_ADDRESS", (String) m_str.get("req_addr"));
                            par_send.put("REQ_ID", m_str.get("id").toString());
                            par_send.put("NOTE", (String) m_str.get("note"));
                            par_send.put("STATUS_REQ", (String) m_str.get("status_req"));
                            //Thread.sleep(5000);
                            userNotifService.sendOtherMess(par_send);
                        }
                    }
                }
            }

            res=reqDAO.inserts(params, user);
            MDC.put("action", "creates");
            n_cp++;
            MDC.put("action", "creates");
logger.info("res="+res);
            sqlCode=(String) params.get("sqlpath");
            n_cp++;
            //Object[] par_val;

            if(sqlCode.equals("update_user_info")){//Для фильтраций одинаковых параметров профиля
                return res;
            }if(sqlCode.equals("create_flat")){//Для фильтраций одинаковых адресов
                return res;
            }else if (sqlCode.equals("ins_build_ksk")||sqlCode.equals("upd_build_ksk")) {
                n_cp++;
                int n_status = (int) Math.round((Double) params.get("status"));
                n_cp++;
                if (n_status == 3) {
                    n_cp = 50;
                    Double n_build = (Double) params.get("buildId");
                    Double n_ksk = (Double) params.get("kskId");
                    n_cp++;
                    int n_user_id = daoutil.IdById("sprav/get_moderator_ksk", n_ksk);
                    n_cp++;
                    int n_old_ksk = daoutil.IdById("sprav/get_ksk_build", n_build);
                    n_cp++;
                    String userEmail = daoutil.textByID("sprav/user_email", Double.valueOf(n_user_id));
                    n_cp++;
                    MDC.put("action", "creates");
                    // Модератору
                    Map<String, String> par_send = new HashMap<>();
                    par_send.put("MSG_CODE", "CNF_ADDR_MOD");
                    par_send.put("MSG_LANG", "RU");
                    par_send.put("ADDRESS", daoutil.textByID("sprav/address_by_build", n_build));
                    String s_owner = daoutil.textByID("sprav/ksk_inf", Double.valueOf(n_old_ksk), "desc_ru");
                    par_send.put("CURR_OWNER", s_owner);
                    par_send.put("NEW_OWNER", daoutil.textByID("sprav/ksk_inf", n_ksk, "desc_ru"));
                    par_send.put("EMAIL", userEmail);
                    n_cp++;
                    //logger.info("call sendOtherMess, userEmail=" + userEmail);
                    userNotifService.sendOtherMess(par_send);

                    // ГП старого КСК
                    par_val = new Object[2];
                    par_val[0] = "mu";
                    par_val[1] = n_old_ksk;
                    n_user_id = daoutil.IdById("sprav/get_ksk_user_by_pos", par_val);
                    //logger.info("mu1 n_user_id=" + n_user_id);
                    userEmail = daoutil.textByID("sprav/user_email", Double.valueOf(n_user_id));
                    par_send.put("EMAIL", userEmail);
                    logger.info("call sendOtherMess to mu1, userEmail=" + userEmail);
                    par_send.put("MSG_CODE", "CNF_ADDR_OLD");
                    userNotifService.sendOtherMess(par_send);

                    // ГП нового КСК
                    par_val[1] = n_ksk;
                    n_user_id = daoutil.IdById("sprav/get_ksk_user_by_pos", par_val);
                    //logger.info("mu2 n_user_id=" + n_user_id);
                    userEmail = daoutil.textByID("sprav/user_email", Double.valueOf(n_user_id));
                    par_send.put("EMAIL", userEmail);
                    logger.info("call sendOtherMess to mu2, userEmail=" + userEmail);
                    par_send.put("MSG_CODE", "CNF_ADDR_NEW");
                    userNotifService.sendOtherMess(par_send);
                }

                Double n_build_id = (Double) params.get("buildId");
                if (n_build_id != 0.0) {
                  String userEmail = "";
                  Double n_disp_id = (Double) params.get("n_disp");
                   if (n_disp_id != 0.0) {
                       userEmail = daoutil.textByID("sprav/get_mail_by_pos_id", n_disp_id);
                   }
                   else {
                       List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_mail_by_ksk_id", (Double) params.get("kskId"));
                       //logger.info("s_position="+s_res.size());
                       for (int i = 0; i < s_res.size(); i++) {
                           //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                           if (i != s_res.size() - 1)
                               userEmail += s_res.get(i)+",";
                           else
                               userEmail += s_res.get(i);



                       }

                   }



                    userEmail = userEmail.replace("{text=","");
                    userEmail = userEmail.replace("}","");

                    logger.info("userEmail="+userEmail);
                  Map<String, String> par_send = new HashMap<>();
                  Map<String, Object> m_str;
                  if (sqlCode.equals("ins_build_ksk")){
                      par_send.put("MSG_CODE", "CIT_INQ_NEW");
                      par_send.put("MSG_LANG", "RU");
                      par_send.put("ACCEPT_KSK", conf.getHost() + "/kskProfile.html#kskProfile?inquiry");
                      par_send.put("EMAIL", userEmail);
                      par_val = new Object[3];
                      par_val[0] = 1;
                      par_val[1] = (Double) params.get("kskId");
                      //par_val[2] = n_disp_id;
					  par_val[2] = n_build_id;
                      //logger.info("n_pos_id1231="+n_pos_id);
                      //logger.info("dasdasdasd="+(Double) params.get("kskId"));
                      n_cp++;
                      List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/inquiry_search_by_build_id", par_val);
                      //logger.info("s_position="+s_res.size());
                      for (int i = 0; i < s_res.size(); i++) {
                          //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                          m_str = s_res.get(i);
                          par_send.put("REG_ADDRESS", (String) m_str.get("address"));
                          par_send.put("REG_FIO", (String) m_str.get("fio_cit"));
                          userNotifService.sendOtherMessToAll(par_send);
                      }

                      // Список не выполненных заявок
                      par_send.put("MSG_CODE", "REQ_NOT_EXEC");
                      par_send.put("REQ_URL", conf.getHost() + "/request.html#reqSearch");
                      par_val = new Object[2];
                      par_val[0] = (Double) params.get("kskId");
					  par_val[1] = n_build_id;
                      n_cp++;
                      s_res=daoutil.queryForMapList("sprav/reqs_list_by_build_id", par_val);
                      n_cp=144;
                      String s_str="";
                      for(int i= 0 ; i < s_res.size(); i++) {
                          n_cp=146;
                          m_str = s_res.get(i);
                          par_send.put("REQ_ADDRESS", (String) m_str.get("req_addr"));
                          par_send.put("REQ_ID", m_str.get("id").toString());
                          par_send.put("NOTE", (String) m_str.get("note"));
                          par_send.put("STATUS_REQ", (String) m_str.get("status_req"));
                          //Thread.sleep(5000);
                          userNotifService.sendOtherMessToAll(par_send);
                      }
                  }
                  else {
                      Double n_disp_old_id = (Double) params.get("n_disp_old");
                      if (n_disp_old_id != n_disp_id) {
                          par_send.put("MSG_CODE", "CIT_INQ_NEW");
                          par_send.put("MSG_LANG", "RU");
                          par_send.put("ACCEPT_KSK", conf.getHost() + "/kskProfile.html#kskProfile?inquiry");
                          par_send.put("EMAIL", userEmail);
                          par_val = new Object[3];
                          par_val[0] = 1;
                          par_val[1] = (Double) params.get("kskId");
                          //par_val[2] = n_disp_id;
                          par_val[2] = n_build_id;
                          n_cp++;
                          List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/inquiry_search_by_build_id", par_val);
                          //logger.info("s_position="+s_res.size());
                          for (int i = 0; i < s_res.size(); i++) {
                              //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                              m_str = s_res.get(i);
                              par_send.put("REG_ADDRESS", (String) m_str.get("address"));
                              par_send.put("REG_FIO", (String) m_str.get("fio_cit"));
                              userNotifService.sendOtherMessToAll(par_send);
                          }

                          // Список не выполненных заявок
                          par_send.put("MSG_CODE", "REQ_NOT_EXEC");
                          par_send.put("REQ_URL", conf.getHost() + "/request.html#reqSearch");
                          par_val = new Object[2];
                          par_val[0] = (Double) params.get("kskId");;
                          par_val[1] = n_build_id;
                          n_cp++;
                          s_res=daoutil.queryForMapList("sprav/reqs_list_by_build_id", par_val);
                          n_cp=144;
                          String s_str="";
                          for(int i= 0 ; i < s_res.size(); i++) {
                              n_cp=146;
                              m_str = s_res.get(i);
                              par_send.put("REQ_ADDRESS", (String) m_str.get("req_addr"));
                              par_send.put("REQ_ID", m_str.get("id").toString());
                              par_send.put("NOTE", (String) m_str.get("note"));
                              par_send.put("STATUS_REQ", (String) m_str.get("status_req"));
                              //Thread.sleep(5000);
                              userNotifService.sendOtherMessToAll(par_send);
                          }
                      }
                          }
                      }
                return res;
                }

			
			else if (sqlCode.equals("upd_addr_activ_by_moderator")){
                par_val = new Object[1];
                par_val[0] =params.get("id");
                List<Map<String, Object>> s_res=daoutil.queryForMapList("sprav/get_cit_emails", par_val);
                for(int i= 0 ; i < s_res.size(); i++) {
                    Map<String, String> par_send = new HashMap<>();
                    par_send.put("MSG_CODE", "CIT_KSK_ACT");
                    par_send.put("MSG_LANG", "RU");
                    logger.debug("get_cit_emails="+s_res.get(i).get("email"));
                    logger.debug("get_cit_addressname="+s_res.get(i).get("addressname"));
                    logger.debug("get_cit_nameofksk="+s_res.get(i).get("nameofksk"));
                    par_send.put("REG_ADDRESS", (String) s_res.get(i).get("addressname"));
                    par_send.put("NAME_KSK", (String) s_res.get(i).get("nameofksk"));
                    par_send.put("EMAIL", (String) s_res.get(i).get("email"));
                    //par_send.put("PUSH", "1");
                    userNotifService.sendOtherMess(par_send);
                }

            }else if (sqlCode.equals("upd_addr_deactiv_by_moderator")){
                par_val = new Object[1];
                par_val[0] =params.get("id");
                List<Map<String, Object>> s_res=daoutil.queryForMapList("sprav/get_cit_emails", par_val);
                for(int i= 0 ; i < s_res.size(); i++) {
                    Map<String, String> par_send = new HashMap<>();
                    par_send.put("MSG_CODE", "CIT_KSK_DACT");
                    par_send.put("MSG_LANG", "RU");
                    logger.debug("get_cit_emails="+s_res.get(i).get("email"));
                    logger.debug("get_cit_addressname="+s_res.get(i).get("addressname"));
                    logger.debug("get_cit_nameofksk="+s_res.get(i).get("nameofksk"));
                    par_send.put("REG_ADDRESS", (String) s_res.get(i).get("addressname"));
                    par_send.put("NAME_KSK", (String) s_res.get(i).get("nameofksk"));
                    par_send.put("EMAIL", (String) s_res.get(i).get("email"));
                    //par_send.put("PUSH", "1");
                    userNotifService.sendOtherMess(par_send);
                }
            }else if (sqlCode.equals("deactivate_ksk_building")){
                par_val = new Object[2];
                par_val[0] =1;
                par_val[1] =params.get("id");
                List<Map<String, Object>> s_res=daoutil.queryForMapList("sprav/get_cit_emails_ksk", par_val);
                for(int i= 0 ; i < s_res.size(); i++) {
                    Map<String, String> par_send = new HashMap<>();
                    par_send.put("MSG_CODE", "KSK_DEACT");
                    par_send.put("MSG_LANG", "RU");
                    par_send.put("REG_ADDRESS", (String) s_res.get(i).get("addressname"));
                    par_send.put("NAME_KSK", (String) s_res.get(i).get("nameofksk"));
                    par_send.put("EMAIL", (String) s_res.get(i).get("email"));
                    userNotifService.sendOtherMess(par_send);
                }
            }else if (sqlCode.equals("deactivate_ksk") ||sqlCode.equals("activate_ksk")) {
                par_val = new Object[2];
                par_val[0] = 1;
                par_val[1] = params.get("id");
                List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_pos_emails_ksk", par_val);
                if (sqlCode.equals("deactivate_ksk")){
                    for (int i = 0; i < s_res.size(); i++) {
                        Map<String, String> par_send = new HashMap<>();
                        par_send.put("MSG_CODE", "POS_KSK_DEACT");
                        par_send.put("MSG_LANG", "RU");
                        par_send.put("NAME_KSK", (String) s_res.get(i).get("nameofksk"));
                        par_send.put("EMAIL", (String) s_res.get(i).get("email"));
                        userNotifService.sendOtherMess(par_send);
                    }
            }else if (sqlCode.equals("activate_ksk")){
                    for (int i = 0; i < s_res.size(); i++) {
                        Map<String, String> par_send = new HashMap<>();
                        par_send.put("MSG_CODE", "POS_KSK_ACT");
                        par_send.put("MSG_LANG", "RU");
                        par_send.put("NAME_KSK", (String) s_res.get(i).get("nameofksk"));
                        par_send.put("EMAIL", (String) s_res.get(i).get("email"));
                        userNotifService.sendOtherMess(par_send);
                    }
                }
		  }

            else if (sqlCode.equals("upd_rel_flats_status")){
                Double n_build_id = (Double) params.get("buildId");
                Map<String, String> par_send = new HashMap<>();
                Map<String, Object> m_str;
                par_send.put("MSG_CODE", "CIT_INQ_NEW");
                par_send.put("MSG_LANG", "RU");
                par_send.put("ACCEPT_KSK", conf.getHost() + "/kskProfile.html#kskProfile?inquiry");
                par_val = new Object[3];
                par_val[0] = 1;
                par_val[1] = (Double) params.get("kskId");
                par_val[2] = n_build_id;

                n_cp++;
                List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/inquiry_search_by_build_id", par_val);
                //logger.info("s_position="+s_res.size());
                for (int i = 0; i < s_res.size(); i++) {
                    //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                    m_str = s_res.get(i);
                    par_send.put("REG_ADDRESS", (String) m_str.get("address"));
                    par_send.put("REG_FIO", (String) m_str.get("fio_cit"));
                    par_val = new Object[2];
                    par_val[0] = (Double) params.get("kskId");
                    par_val[1] = n_build_id;
                    //String userEmail = daoutil.textByID("sprav/get_mail_by_pos_id", m_str.get("pos_id"));
                    String userEmail = daoutil.textByID("sprav/get_mail_disp_by_ksk_and_build_id", par_val);

                    if (userEmail == null) {
                        userEmail = "";
                        List<Map<String, Object>> s_res_1 = daoutil.queryForMapList("sprav/get_mail_by_ksk_id", (Double) params.get("kskId"));
                        for (int j = 0; j < s_res_1.size(); j++) {
                            //logger.info("s_position=fdfsdfsdfsdfsdfsdfsdfdsfdsfsdf");
                            if (j != s_res_1.size() - 1)
                                userEmail += s_res_1.get(j) + ",";
                            else
                                userEmail += s_res_1.get(j);


                        }
                    }

                    userEmail = userEmail.replace("{text=","");
                    userEmail = userEmail.replace("}","");
                    logger.info("userEmail = "+userEmail);
                    par_send.put("EMAIL", userEmail);
                    //logger.info("s_position="+userEmail);
                    userNotifService.sendOtherMessToAll(par_send);
                }
            }
            else if (sqlCode.equals("insert_t_notification_for_mod")){
                String userFIO = (String) params.get("userFIO");
                String userAddr = (String) params.get("addrForDeactive");
                String kskName = (String) params.get("kskName");
                String text_mail = (String) params.get("text_mail");
                //logger.info("gogogogoggogogogogogogogogogogog");
                String userEmail = daoutil.textByID("sprav/get_mail_by_user_id", (Double) params.get("userIds"));
                //logger.info("userEmail_moder(11)="+userEmail);
                Map<String, String> par_send = new HashMap<>();
                par_send.put("MSG_CODE", "NOTIF_TO_MOD_FOR_DEAC");
                par_send.put("MSG_LANG", "RU");
                par_send.put("DEACTIVE_ADDRESS", userAddr);
                par_send.put("USER_FIO", userFIO);
                par_send.put("KSK_NAME", kskName);
                par_send.put("TEXT_MAIL", text_mail);
                par_send.put("EMAIL", userEmail);
                //logger.info("userEmail_moder="+userEmail);
                userNotifService.sendOtherMess(par_send);

            }else if(sqlCode.equals("ins_employees_serv")){
                par_val = new Object[3];
                par_val[0] = params.get("kskId");
                par_val[1] = params.get("login");
                par_val[2] = params.get("position_types");
                List<Map<String, Object>> s_res_1 = daoutil.queryForMapList("sprav/get_serv_emps_lang_text",  par_val);
                String position="";

                for (int i =0; i < s_res_1.size(); i++){
                    position=position + s_res_1.get(i).get("lang_text").toString();
                    if(i<s_res_1.size()-1){
                        position=position+",";
                    }
                }

                if(s_res_1.get(0).get("passw").equals("orapas$123")){
                OneTimeCodeForEmail otc = oneTimeCodeDAOEmail.createCode( params.get("login").toString() );
                String code = otc.getCode();
                String url = conf.getLoginHost() + "/reset_password.html?code=" + code;

                Map<String, String> par_send = new HashMap<>();
                par_send.put("MSG_CODE", "CR_NEW_EMP_SERV");
                par_send.put("MSG_LANG", "RU");
                par_send.put("COMP_ROLE", position);
                par_send.put("COMP_NAME",  s_res_1.get(0).get("comp_name").toString());
                par_send.put("INSTRUCTIONS_URL", url);
                par_send.put("EMAIL", params.get("login").toString());
                userNotifService.sendOtherMess(par_send);
                }else{
                    Map<String, String> par_send = new HashMap<>();
                    par_send.put("MSG_CODE", "CR_EMP_SERV");
                    par_send.put("MSG_LANG", "RU");
                    par_send.put("COMP_ROLE", position);
                    par_send.put("COMP_NAME",  s_res_1.get(0).get("comp_name").toString());
                    par_send.put("INSTRUCTIONS_URL", conf.getHost());
                    par_send.put("EMAIL", params.get("login").toString());
                    userNotifService.sendOtherMess(par_send);
                }
            }
        }
        catch (Exception e) {
            MDC.put("action", "creates");
            logger.error("n_cp = "+ n_cp + ", exception,  " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
        logger.info("end creates, res="+res);
        return res;
    }   //  end creates

    @SpringTransactional
    public String execFunc(Map<String, Object> params, User user) {
        MDC.put("action", "creates");
        logger.debug("Reqdreaf execFunc,params="+params.toString());
        String res, sqlCode;
        int n_cp=44;
        try {

            sqlCode=(String) params.get("sqlpath");
            n_cp++;
            Object[] par_val;


            res=reqDAO.execFunction(params, user);
            MDC.put("action", "creates");
            n_cp++;
            MDC.put("action", "creates");
            logger.info("res="+res);
            sqlCode=(String) params.get("sqlpath");
            n_cp++;

            if(sqlCode.equals("execFunc")){//Для фильтраций одинаковых адресов
                return res;
            }


        }
        catch (Exception e) {
            MDC.put("action", "creates");
            logger.error("n_cp = "+ n_cp + ", exception,  " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
        logger.info("end creates, res="+res);
        return res;
    }   //  end execFunc

    @SpringTransactional
    public String execFunc3(Map<String, Object> params, User user) {
        MDC.put("action", "creates");
        logger.debug("Reqdreaf execFunc,params="+params.toString());
        String res, sqlCode;
        int n_cp=44;
        try {

            sqlCode=(String) params.get("sqlpath");
            n_cp++;
            Object[] par_val;


            res=reqDAO.execFunction3(params, user);
            MDC.put("action", "creates");
            n_cp++;
            MDC.put("action", "creates");
            logger.info("res="+res);
            sqlCode=(String) params.get("sqlpath");
            n_cp++;

            if(sqlCode.equals("execFunc")){//Для фильтраций одинаковых адресов
                return res;
            }


        }
        catch (Exception e) {
            MDC.put("action", "creates");
            logger.error("n_cp = "+ n_cp + ", exception,  " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
        logger.info("end creates, res="+res);
        return res;
    }   //  end execFunc


    @Override
    @SpringTransactional
    public String req_hist(Map<String, Object> params, User user) {
        MDC.put("action", "req_hist");
        Integer nk;
        Object[] par_val;
        Integer n_cp = 0;
        Integer check_reg_status;
        String sqlPath = params.get("sqlpath").toString();
        logger.info("sqlPath=" + sqlPath);
        logger.info("params=" + params.toString());
        //Object[] values;
        Map<Integer, Object> par_req = new TreeMap<Integer, Object>();
        Map<String, Object> par_other = new HashMap<>();

        //Map<String, Object> par_hist = new TreeMap<String, Object>();
        ReqHistory reqHist = new ReqHistory();
        List<Map<String, Object>> res;
        Boolean b_send_email=false;
        int sid =0;
        //String s_mail_disp;
        int cur_stat=1;
        Double t_request_id = (Double) params.get("t_request_id");

        try {
            n_cp = 65;
            Date dead_line=null;
            if (params.get("req_flat")!=null) {
                logger.info("req_flat=" + params.get("req_flat"));
                params.put("req_address", daoutil.textByID("sprav/address_by_flat", (Double) params.get("req_flat")));
                logger.info("req_address=" + params.get("req_address"));
            }
            n_cp++;
            if (params.get("req_subtype_id")!=null){
                logger.info("req_subtype=" + params.get("req_subtype_id"));
                params.put("req_type", daoutil.textByID("sprav/req_type_by_subtype", (Double) params.get("req_subtype_id")));
                logger.info("req_type=" + params.get("req_type"));
            }
            n_cp++;
            if (params.get("req_status")!=null) {
                cur_stat=(int) Math.round((Double) params.get("req_status"));
            }
            logger.info("t_request_id=" + t_request_id);
            if (sqlPath.equals("insert_req_his")) {         // Назначение, исполнение
                n_cp = 74;
                par_req.clear();
                nk = 0;
                n_cp++;
                par_req.put(nk, "update_req_his_del_by_req");
                nk++;
                par_req.put(nk, 1);
                nk++;
                par_req.put(nk, t_request_id);
                nk++;
                par_req.put(nk, 0);
                n_cp++;
                //   Проставляем всем записям признак удален
                n_cp++;
                reqDAO.updates(par_req, true);
                MDC.put("action", "req_hist");
                n_cp++;
            }
            else if (sqlPath.equals("update_req_close")){       //   Закрытие заявки
                n_cp = 99;
                reqHist.setT_request_id(Math.round(t_request_id));
                n_cp++;
                reqHist.setT_req_status_id(cur_stat);
                n_cp++;
                reqHist.setT_note((String) params.get("rate_text"));
                reqHist.setDisp_id(0);
                reqHist.setT_position_id(daoutil.IdById("sprav/req_hist_disp",t_request_id));

                check_reg_status = daoutil.IdById("sprav/get_reg_status_id", t_request_id);


                if (check_reg_status ==1) {
                    reqHist.setT_position_id(daoutil.IdById("sprav/get_sysuser_posID",1));
                    logger.info("YES");
                    String user_mail = daoutil.textByID("sprav/get_disp_mail_by_rec_id",t_request_id);
                     if (user_mail ==null) {
                         logger.info("1414141414141414141");
                         user_mail = "";
                         par_val = new Object[1];
                         par_val[0] = t_request_id;
                         List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_all_disp_by_rec_id", par_val);
                         //logger.info("s_position="+s_res.size());
                         for (int i = 0; i < s_res.size(); i++) {
                             //logger.info("t_request_id 122313212131");
                             if (i != s_res.size() - 1)
                                 user_mail += s_res.get(i)+",";
                             else
                                 user_mail += s_res.get(i);
                         }
                     }
                    Map<String, String> par_send = new HashMap<>();
                    user_mail = user_mail.replace("{text=","");
                    user_mail = user_mail.replace("}","");
                    logger.info("user_mail = "+user_mail);
                    if (user_mail != null) {
                        par_send.put("MSG_CODE", "CLOSE_REQ");
                        par_send.put("MSG_LANG", "RU");
                        par_send.put("T_REQUEST_ID", Long.toString(Math.round(t_request_id)));
                        par_send.put("EMAIL", user_mail);
                        userNotifService.sendOtherMessToAll(par_send);
                    }
                }
                 else {
                        reqHist.setT_position_id(daoutil.IdById("sprav/req_hist_disp", t_request_id));
                        if(reqHist.getT_position_id() == 0||reqHist.getT_position_id() == null) {
                            reqHist.setT_position_id(daoutil.IdById("sprav/get_sysuser_posID",1));
                        }
                    }




                reqHist.setUser_id(user.getUserId());
                n_cp++;
                reqDAO.reqHist(reqHist, "insert_req_his_by_user");
                MDC.put("action", "req_hist");

                // Запись истории
                par_req.clear();
                nk = 0;
                par_req.put(nk, sqlPath);
                nk++;
                n_cp++;
                par_req.put(nk, reqHist.getT_req_status_id());
                nk++;
                n_cp++;
                par_req.put(nk, params.get("rate_val"));
                nk++;
                n_cp++;
                par_req.put(nk, reqHist.getT_note());
                nk++;
                n_cp++;
                par_req.put(nk, t_request_id);
                n_cp++;
                MDC.put("action", "req_hist");
                return reqDAO.updates(par_req, false);
            }
            else if (sqlPath.equals("reopen_req")){             // Повторное открытие заявки
                n_cp = 137;
                logger.info("t_request_id=" + t_request_id);
                n_cp++;
                reqHist.setT_request_id(Math.round(t_request_id));
                n_cp++;
                logger.info("req_status=" + params.get("req_status"));
                n_cp++;
                reqHist.setT_req_status_id(cur_stat);
                reqHist.setT_note((String) params.get("reason_note"));
                reqHist.setDisp_id(0);
                reqHist.setT_position_id(daoutil.IdById("sprav/req_hist_disp",t_request_id));
                MDC.put("action", "req_hist");
                n_cp++;
                logger.info("user.getUserId()=" + user.getUserId());
                reqHist.setUser_id(user.getUserId());
                n_cp++;
                sid= Integer.parseInt(reqDAO.reqHist(reqHist, "insert_req_his_by_user"));
                MDC.put("action", "req_hist");
                // изменение статуса заявки
                par_req.clear();
                n_cp++;
                nk = 0;
                par_req.put(nk, "update_req_status");
                nk++;
                par_req.put(nk, reqHist.getT_req_status_id());
                nk++;
                par_req.put(nk, reqHist.getT_request_id());
                n_cp++;
                reqDAO.updates(par_req, true);
                MDC.put("action", "req_hist");
                // Оповещение диспетчера
                n_cp++;
                logger.info("***** send to disp ****");
                reqHist.setCrReqMail(daoutil.textByID("sprav/position_email", reqHist.getT_position_id().doubleValue()));
                reqHist.setT_req_status_id(cur_stat);
                sendNotifUser(reqHist, params, false);
                // Оповещение автора заявки
                n_cp++;
                reqHist.setExec_Email(reqHist.getCrReqMail());
                reqHist.setCrReqMail(daoutil.textByID("sprav/user_email",(Double) params.get("t_user_id")));
                if (!reqHist.getCrReqMail().equals(reqHist.getExec_Email())){
                    logger.info("***** send to user ****");
                    logger.info("Mails ["+reqHist.getCrReqMail()+"]<>["+reqHist.getExec_Email()+"]");
                    sendNotifUser(reqHist, params, true);
                }
                n_cp++;
                return String.valueOf(sid);
            }
            else if (sqlPath.equals("reject_req")){       //   Отклонение заявки
                par_req.clear();
                nk = 0;
                par_req.put(nk, "update_req_reject");
                nk++;
                n_cp = 206;
                par_req.put(nk, params.get("req_status"));
                nk++;
                n_cp++;
                par_req.put(nk, params.get("req_reasons"));
                nk++;
                par_req.put(nk, t_request_id);
                // Меняем статус заявки
                n_cp++;
                reqDAO.updates(par_req, false);
                MDC.put("action", "req_hist");

                n_cp++;
                reqHist.setT_request_id(Math.round(t_request_id));
                n_cp++;
                reqHist.setT_req_status_id(cur_stat);
                n_cp++;
                reqHist.setT_note((String) params.get("reason_note"));
                int disp_id = daoutil.IdById("sprav/disp_id", user.getUserId().doubleValue());
                if (disp_id<=0) {
                    disp_id = daoutil.IdById("sprav/spec_id", user.getUserId().doubleValue());
                }
                logger.info("disp_id=" + disp_id);
                reqHist.setT_position_id(disp_id);
                n_cp++;
                // Заполняем историю заявки
                sid=Integer.parseInt(reqDAO.reqHist(reqHist, sqlPath));
                // Оповещение автора заявки
                n_cp = 234;
                reqHist.setCrReqMail(daoutil.textByID("sprav/user_email",(Double) params.get("t_user_id")));
                reqHist.setT_req_status_id(cur_stat);
                sendNotifUser(reqHist, params, true);
                n_cp = 246;
                return String.valueOf(sid);
            }
            n_cp = 249;
            Object cur_hist = params.get("cur_hist");
            logger.info("Parse cur_hist");
            if (cur_hist==null) {
                logger.info("cur_hist is null");
            }
            else {
                n_cp = 263;
                logger.info("cur_hist=" + cur_hist.toString());
                //ArrayList arr1;
                List<Map<String, Object>> arr1;
                Map<String, Object> m_str;
                arr1 = (ArrayList) cur_hist;
                reqHist.setSid(sid);
                for(int i= 0 ; i < arr1.size(); i++){
                    m_str = (Map<String, Object>) arr1.get(i);
                    n_cp = 272;
                    reqHist.setId(Math.round((Double) m_str.get("id")));
                    n_cp++;
                    if (reqHist.getId()!=0) {
                        n_cp++;
                        sid = Integer.parseInt(reqDAO.reqHist(reqHist, sqlPath));
                    }
                    else {
                        n_cp = 280;
                        reqHist.setT_request_id(Math.round(t_request_id));
                        reqHist.setT_position_id((int) Math.round((Double) m_str.get("t_position_id")));
                        reqHist.setT_req_status_id((int) Math.round((Double) m_str.get("t_req_status_id")));
                        reqHist.setT_note((String) m_str.get("note"));
                        n_cp++;
                        int disp_id = daoutil.IdById("sprav/disp_id", user.getUserId().doubleValue());
                        if (disp_id>0) {
                            logger.info("disp_id=" + disp_id);
                            reqHist.setDisp_id(disp_id);
                        }
                        n_cp++;
                        Object d_history = m_str.get("d_history");
                        d_history = daoutil.StrtoDate(d_history.toString());
                        reqHist.setD_history((Date) d_history);
                        n_cp++;
                        if (sqlPath.equals("exec_req_his")) {
                            logger.info("sid="+m_str.get("sid"));
                            sid =(int) Math.round((Double) m_str.get("sid"));
                            logger.info("sid_num="+sid);
                            reqHist.setSid(sid);
                        }
                        n_cp++;
                        reqHist.setUser_id(user.getUserId());
                        sid = Integer.parseInt(reqDAO.reqHist(reqHist, sqlPath));

                        //----------------добавляем файлы
                        if (sqlPath.equals("exec_req_his")) {
                            Long table_id, stab_id;
                            table_id = Long.parseLong(Integer.toString(sid));
                            logger.info("adlet table_id=" + sid);
                            Object files_object = m_str.get("exec_files");
                            //logger.info("adlet files_object=" + files_object.toString());
                            List<Map<String, Object>> files_list;
                            Map<String, Object> files_map;

                            files_list = (ArrayList) files_object;
                            logger.info(" adlet files_list["+i+"].size()=" + files_list.size());
                            //logger.info(" adlet files_list.toString()=" + files_list.toString());

                            for(int j= 0 ; j < files_list.size(); j++) {
                                files_map = (Map<String, Object>) files_list.get(j);
                                //logger.info(files_map.get("name").toString());
                                String ftype="", inType;
                                FileOutputStream outFile = null;
                                try {
                                    byte[] imageByte = org.apache.commons.codec.binary.Base64.decodeBase64((String) files_map.get("content"));
                                    inType = (String) files_map.get("type");
                                    if (inType.equals("image/png")) {
                                        ftype = ".png";
                                    } else if (inType.equals("image/jpeg")) {
                                        ftype = ".jpg";
                                    } else if (inType.equals("image/jpg")) {
                                        ftype = ".jpg";
                                    } else if (inType.equals("image/gif")) {
                                        ftype = ".gif";
                                    } else if (inType.equals("image/tiff")) {
                                        ftype = ".tiff";
                                    } else if (inType.equals("image/psd")) {
                                        ftype = ".psd";
                                    } else if (inType.equals("application/pdf")) {
                                        ftype = ".pdf";
                                    }
                                    logger.info(" adlet Type defined{}=" + ftype);
                                    DocFile docFile = new DocFile();
                                    docFile.setCreatedBy(user.getUserId());
                                    docFile.setFileName((String) files_map.get("name"));
                                    docFile.setFileType((String) files_map.get("type"));
                                    docFile.setFileSize(Math.round((Double) files_map.get("size")));
                                    docFile.setPath(conf.getFileStorePath());
                                    docFile.setHashValue((String) files_map.get("content"));
                                    docFile.setIsSystemGenerated(false);
                                    docFile.setTableId(table_id);
                                    docFile.setTableName((String) files_map.get("table_name"));
                                    if (!ftype.isEmpty()) {
                                        MDC.put("action", "create");
                                        logger.info("docFile=" + docFile.toString());
                                        Long fileId = docFileDAO.create(docFile);
                                        logger.info("fileId{}=" + fileId);
                                        String directory = docFile.getPath() + fileId + ftype;
                                        File f = new File(directory);
                                        outFile = new FileOutputStream(f);
                                        outFile.write(imageByte);
                                        ftype = "";
                                    }
                                    outFile.close();
                                }
                                catch (IOException e) {
                                    logger.info("ERROR UPLOAD = "+e.toString());
                                    throw null;
                                }
                            }

                        }
                        //-----------------------------------
                        if (sqlPath.equals("insert_req_his")) {
                            // Оповещение исполнителя
                            n_cp=306;
                            logger.info("call sendToExecsendToExec");
                            n_cp++;
                            //= new Date();
                            if (params.get("dead_line") != null) {
                                n_cp++;
                                logger.info("dead_line=" + params.get("dead_line").toString());
                                n_cp++;
                                dead_line = daoutil.StrtoDate(params.get("dead_line").toString());
                            }
                            reqHist.setExec_Email(daoutil.textByID("sprav/position_email", reqHist.getT_position_id().doubleValue()));
                            logger.info("reqHist.getExec_Email()=" + reqHist.getExec_Email());
                            n_cp++;
                            reqDAO.sendToExec(reqHist.getExec_Email(), reqHist.getT_request_id(), (String) params.get("req_address"), (String) params.get("req_type"), dead_line);
                            MDC.put("action", "req_hist");
                        }
                    }
                }  // end for
            }
            if (sqlPath.equals("insert_req_his")) {
                n_cp = 327;
                par_req.clear();
                nk=0;
                par_req.put(nk, "update_req");
                nk++;
                par_req.put(nk, params.get("t_req_priority_id"));
                logger.info("t_req_priority_id=" + params.get(nk));
                nk++;
                par_req.put(nk, daoutil.StrtoDate(params.get("dead_line").toString()));
                logger.info("dead_line=" + params.get(nk));
                nk++;
                /*par_req.put(nk, params.get("req_subtype_id"));
                logger.info("req_subtype_id=" + params.get(nk));
                nk++;*/
                if (cur_hist!=null) {
                    par_req.put(nk, params.get("req_status"));
                    b_send_email=true;
                }
                else
                    par_req.put(nk, params.get("t_req_status_old"));
                logger.info("req_status=" + params.get(nk));
                nk++;
                par_req.put(nk, t_request_id);
                logger.info("t_request_id=" + params.get(nk));
                n_cp++;
                reqDAO.updates(par_req, false);
                MDC.put("action", "req_hist");
            }
            else if (sqlPath.equals("exec_req_his")){
                int cur_stat_old=(int) Math.round((Double) params.get("t_req_status_old"));
                cur_stat=cur_stat_old;
                n_cp = 358;
                par_other.clear();
                par_other.put("sqlpath", "sprav/req_status_by_hist");
                par_other.put("t_request_id", t_request_id);
                res = daoutil.queryForMapList(par_other);
                MDC.put("action", "req_hist");
                logger.info("res=" + res.toString());
                if (res.size()>0) {
                    cur_stat = Integer.parseInt(res.get(0).get("stat_id").toString());
                }
                logger.info("cur_stat=" + cur_stat);
                logger.info("cur_stat_old=" + cur_stat_old);
                if (cur_stat!=cur_stat_old){
                    // изменение статуса заявки
                    par_req.clear();
                    nk = 0;
                    par_req.put(nk, "update_req_status");
                    nk++;
                    par_req.put(nk, cur_stat);
                    nk++;
                    par_req.put(nk, t_request_id);
                    n_cp++;
                    reqDAO.updates(par_req, true);
                    MDC.put("action", "req_hist");
                    b_send_email=true;
                }
            }
            //if (sqlPath.equals("insert_req_his")) {
            if (b_send_email) {
                // Оповещение автора заявки
                n_cp=388;
                reqHist.setCrReqMail(daoutil.textByID("sprav/user_email",(Double) params.get("t_user_id")));
                reqHist.setT_req_status_id(cur_stat);
                sendNotifUser(reqHist, params, true);
            }
            return String.valueOf(sid);
        }  // end try
        catch (Exception e) {
            MDC.put("action", "req_hist");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
    }

    public void sendNotifUser(ReqHistory reqHist, Map<String, Object> params, boolean cit) {
        MDC.put("action", "sendNotifUser");
        int n_cp=412;
        try {
            if (reqHist.getCrReqMail()!=null){
                logger.info("req_hist, send mess to = " + reqHist.getCrReqMail() + ", cur_stat="+reqHist.getT_req_status_id());
                n_cp = 392;
                String s_url=conf.getHost();
                if (cit){
                    s_url+= "/citizenrequest.html#citReqSearch";
                }
                else {
                    s_url+= "/request.html#reqSearch";
                }
                Map<String, String> par_send = new HashMap<>();
                par_send.put("MSG_LANG","RU");
                par_send.put("REQ_ID",reqHist.getT_request_id().toString());
                par_send.put("EMAIL",reqHist.getCrReqMail().toString());
                par_send.put("INSTRUCTIONS_URL",s_url);
                Object[] values;
                switch (reqHist.getT_req_status_id()){
                    case 4:     // исполнено
                        n_cp = 431;
                        par_send.put("MSG_CODE","REQ_EXECUTED");
                        break;
                    case 7:     // повторно открыто
                        n_cp = 436;
                        par_send.put("MSG_CODE","REQ_REOPEN");
                        par_send.put("REQ_ADDRESS",(String) params.get("req_address"));
                        par_send.put("REQ_TYPE",(String) params.get("req_type"));
                        break;
                    case 8:     // отклонено
                        par_send.put("MSG_CODE","REQ_REJECTED");
                        values = new Object[]{params.get("req_reasons"), 1};
                        par_send.put("REASON_TEXT", daoutil.textByID("sprav/cur_reject_reason",values));
                        par_send.put("REASON_NOTE",reqHist.getT_note());
                        par_send.put("INSTRUCTIONS_URL",s_url);
                        break;
                    default:
                        n_cp = 437;
                        values = new Object[]{reqHist.getT_req_status_id(), 1};
                        n_cp = 439;
                        String stat_text=daoutil.textByID("sprav/req_status_by_id",values);
                        n_cp = 441;
                        par_send.put("MSG_CODE","REQ_STATUS");
                        par_send.put("CUR_STAT",stat_text);
                }   // end switch
                //userService.sendNotif(par_send);
                //par_send.put("PUSH", "1");
                userNotifService.sendOtherMess(par_send);
            }
            else {
                logger.info("req_hist, Email is null");
            }
        }
        catch (Exception e) {
            MDC.put("action", "sendNotifUser");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
    }

    @SpringTransactional
    public String ins_ksk(Map<String, Object> params, User user) {
        MDC.put("action", "ins_ksk");
        int n_cp=439;
        String s_ksk_id="";
        try {
            int n_pos_id=daoutil.IdById("sprav/position_typeId_by_code","mu");
            logger.info("params="+params.toString());
            n_cp++;
            Object[] new_params = new Object[15];
            int n_id  = 0;
            n_cp++;
            new_params[n_id] = params.get("name_rus");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("ksk_bin");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("phone_num");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("ksk_street");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("ksk_building");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("ksk_fraction");
            n_id++;
            n_cp++;
            new_params[n_id] ="t_ksk";
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("name_gos");
            n_id++;
            n_cp++;
            new_params[n_id] = n_pos_id;
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("ownership");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("main_user");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("pr_user");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("chief_user");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("flats_count");
            n_id++;
            n_cp++;
            new_params[n_id] = params.get("userId");



            n_cp=454;
            KeyHolder kh =daoutil.inserts("requests/insert_ksk", new_params);
            n_cp++;

            s_ksk_id=kh.getKeys().get("status").toString();
            logger.info("s_ksk_id="+s_ksk_id);

           /* Object[] values;
            new_params = new Object[4];
            new_params[0] = n_ksk;
            new_params[1] = "t_ksk";
            new_params[2] = 2;
            new_params[3] = params.get("name_gos");
            n_cp=470;*/
          /*  daoutil.inserts("requests/insert_lang_text", new_params);

            new_params[2] = 1;
            new_params[3] = params.get("name_rus");
            n_cp=475;
            daoutil.inserts("requests/insert_lang_text", new_params);*/


            n_cp=487;
            Map<String, String> par_send = new HashMap<>();
            par_send.put("MSG_CODE","KSK_REG");
            par_send.put("MSG_LANG","RU");
            par_send.put("KSK_NAME",(String) params.get("name_rus"));
            par_send.put("KSK_BIN",(String) params.get("ksk_bin"));
            par_send.put("EMAIL",daoutil.textByID("sprav/user_email",(Double) params.get("main_user")));
            userNotifService.sendOtherMess(par_send);
            //values = new Object[]{reqHist.getT_req_status_id(), 1};
            return s_ksk_id;
        }
        catch (Exception e) {
            MDC.put("action", "ins_ksk");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }

    }

    @SpringTransactional
    public void cit_relation(Map<String, Object> params, User user) {
        MDC.put("action", "cit_relation");
        int n_cp=510;
        try {
            logger.info("params="+params.toString());
            Object[] new_params;
            String sqlpath= (String) params.get("sqlpath");

            if (sqlpath.equals("cit_close_relation")){
                n_cp=518;
                Object cur_hist = params.get("cur_hist");
                new_params = new Object[5];
                n_cp++;
                logger.info("cur_hist=" + cur_hist.toString());
                List<Map<String, Object>> arr1;
                Map<String, Object> m_str;
                n_cp++;
                arr1 = (ArrayList) cur_hist;
                logger.info("arr1.size()=" + arr1.size());
                for(int i= 0 ; i < arr1.size(); i++) {
                    n_cp=527;
                    m_str = (Map<String, Object>) arr1.get(i);
                    int n_id = 0;
                    n_cp++;
                    new_params[n_id] = daoutil.StrtoDate(m_str.get("dat_end").toString());
                    n_id++;
                    n_cp++;
                    new_params[n_id] = m_str.get("status");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("t_req_reasons");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("note");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = m_str.get("id");
                    daoutil.updates("requests/"+sqlpath, new_params);
                    n_cp=539;
                    String s_mail=daoutil.textByID("sprav/user_email",(Double) m_str.get("t_user_id"));
                    n_cp++;
                    logger.info("t_user_id="+m_str.get("t_user_id"));
                    n_cp++;
                    logger.info("s_mail="+s_mail);
                    n_cp++;
                    if (s_mail!=null){
                        n_cp++;
                        Map<String, String> par_send = new HashMap<>();
                        par_send.put("MSG_CODE","CIT_INQ_REJ");
                        par_send.put("MSG_LANG","RU");
                        par_send.put("ADDRESS",daoutil.textByID("sprav/address_by_flat", (Double) params.get("flat_id")));
                        par_send.put("NOTE",(String) params.get("note"));
                        par_send.put("EMAIL", s_mail);
                        //par_send.put("PUSH", "1");
                        logger.info("EMAIL="+par_send.get("EMAIL"));
                        userNotifService.sendOtherMess(par_send);
                    }
                    logger.info("End proc");
                }
            }
            else if (sqlpath.equals("cit_appr_relation")){
                n_cp=563;
                int n_rel_type=(int) Math.round((Double) params.get("rel_type_id"));
                n_cp++;
                int n_rel_id=(int) Math.round((Double) params.get("id"));
                n_cp++;
                String s_mail;
                Map<String, String> par_send = new HashMap<>();
                par_send.put("MSG_LANG","RU");
                par_send.put("ADDRESS",daoutil.textByID("sprav/address_by_flat", (Double) params.get("flat_id")));
                logger.info("ADDRESS="+par_send.get("ADDRESS"));
                logger.info("n_rel_type="+n_rel_type);
                if (n_rel_type==2 && params.get("cur_own_rel_id")!=null) {
                    n_cp=571;
                    new_params = new Object[3];
                    n_cp++;
                    logger.info("date_start="+params.get("date_start"));
                    new_params[0] = daoutil.StrtoDate((String) params.get("date_start"));
                    new_params[1] = 4;
                    logger.info("cur_own_rel_id="+params.get("cur_own_rel_id"));
                    new_params[2] = (int) Math.round((Double) params.get("cur_own_rel_id"));
                    n_cp++;
                    daoutil.updates("requests/cit_close_relation", new_params);
                    s_mail=daoutil.textByID("sprav/user_email",(Double) params.get("cur_own_id"));
                    if (s_mail!=null){
                        n_cp++;
                        par_send.put("MSG_CODE","CIT_ADDR_CLOSE");
                        par_send.put("NOTE",(String) params.get("note"));
                        logger.info("NOTE="+par_send.get("NOTE"));
                        par_send.put("EMAIL", s_mail);
                        //par_send.put("PUSH", "1");
                        logger.info("EMAIL="+par_send.get("EMAIL"));
                        userNotifService.sendOtherMess(par_send);
                    }
                }
                n_cp=592;
                new_params = new Object[2];
                new_params[0] = 1;
                new_params[1] = n_rel_id;
                n_cp++;
                daoutil.updates("requests/upd_relation_stat", new_params);
                n_cp=598;
                s_mail=daoutil.textByID("sprav/user_email",(Double) params.get("t_user_id"));
                if (s_mail!=null){
                    n_cp++;
                    par_send.put("MSG_CODE","CIT_INQ_APPR");
                    par_send.put("EMAIL", s_mail);
                    //par_send.put("PUSH", "1");
                    logger.info("EMAIL="+par_send.get("EMAIL"));
                    userNotifService.sendOtherMess(par_send);
                }
            }
        }
        catch (Exception e) {
            MDC.put("action", "ins_ksk");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
    }

    @SpringTransactional
    public String rep_insert(Map<String, Object> params, User user) {
        MDC.put("action", "rep_insert");
        int n_cp=796;
        int n_rep_id=0;
        Map<String, Object> rec_line;
        try {
            logger.info("params="+params.toString());
            Object[] new_params;
            String sqlpath= (String) params.get("sqlpath");
            if (sqlpath.equals("rep_ins")){
                n_cp=803;
                new_params = new Object[8];
                int n_id = 0;
                n_cp++;
                new_params[n_id] = params.get("t_rep_type_id");
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("t_ksk_id");
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("userId");
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("t_buk");
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("t_ruk");
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("t_build");
                n_id++;
                n_cp++;
                new_params[n_id] = daoutil.StrtoDate(params.get("date_start").toString());
                n_id++;
                n_cp++;
                new_params[n_id] = daoutil.StrtoDate(params.get("date_end").toString());
                KeyHolder kh = daoutil.updates("reports/"+sqlpath, new_params);
                n_cp++;
                n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                MDC.put("action", "rep_insert");
                logger.info("n_rep_id="+n_rep_id);

                rec_line = (Map<String, Object>) params.get("rec_line");

                n_cp++;
                logger.info("rec_line="+rec_line.toString());
                new_params = new Object[3];
                for(Iterator<Map.Entry<String, Object>> s_line = rec_line.entrySet().iterator(); s_line.hasNext(); ) {
                    Map.Entry<String, Object> entry = s_line.next();
                    n_id = 0;
                    n_cp++;
                    new_params[n_id] = n_rep_id;
                    n_id++;
                    n_cp++;
                    new_params[n_id] = entry.getKey();
                    n_id++;
                    n_cp++;
                    new_params[n_id] = entry.getValue();
                    daoutil.updates("reports/rep_line_val_ins", new_params);
                }
            }
            else if (sqlpath.equals("rep_upd")){
                n_cp=820;
                logger.info("rep_upd");
                rec_line = (Map<String, Object>) params.get("rec_line");
                if (rec_line!=null){
                    n_cp++;
                    new_params = new Object[7];
                    //UPDATE ch_ksk.t_reports SET date_reg=CURRENT_TIMESTAMP, t_user_id=?, t_buk=?, t_ruk=?
                    //WHERE id=?;
                    int n_id = 0;
                    n_cp++;
                    new_params[n_id] = params.get("userId");
                    logger.info("userId="+new_params[0]);
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("t_buk");
                    logger.info("t_buk="+new_params[1]);
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("t_ruk");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("t_build");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = daoutil.StrtoDate(params.get("date_start").toString());
                    n_id++;
                    n_cp++;
                    new_params[n_id] = daoutil.StrtoDate(params.get("date_end").toString());;
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("id");
                    KeyHolder kh = daoutil.updates("reports/"+sqlpath, new_params);
                    n_cp++;
                    n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                    logger.info("n_rep_id="+n_rep_id);

                    n_cp++;
                    Object[] new_par2;

                    new_par2 = new Object[1];
                    new_par2[0] = n_rep_id;

                    n_cp++;
                    kh = daoutil.deletes("reports/rep_line_val_del", new_par2);
                    n_cp++;
                    //n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());

                    n_cp++;
                    logger.info("rec_line2="+rec_line.toString());
                    new_params = new Object[3];
                    for(Iterator<Map.Entry<String, Object>> s_line = rec_line.entrySet().iterator(); s_line.hasNext(); ) {
                        Map.Entry<String, Object> entry = s_line.next();
                        n_id = 0;
                        n_cp++;
                        new_params[n_id] = n_rep_id;
                        n_id++;
                        n_cp++;
                        new_params[n_id] = entry.getKey();
                        n_id++;
                        n_cp++;
                        new_params[n_id] = entry.getValue();
                        daoutil.updates("reports/rep_line_val_ins", new_params);
                    }
                }
                else {
                    logger.info("rep_insert rec_line is null");
                    n_cp=900;
                    new_params = new Object[2];
                    int n_id = 0;
                    n_cp++;
                    new_params[n_id] = params.get("t_rep_stat");
                    logger.info("new_params[" + n_id + "]="+new_params[n_id]);
                    //n_cp++;
                    //new_params[n_id] = params.get("userId");
                    //logger.info("userId="+new_params[0]);
                    //n_id++;
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("id");
                    logger.info("new_params[" + n_id + "]="+new_params[n_id]);
                    KeyHolder kh = daoutil.updates("reports/rep_upd_status", new_params);
                    n_cp++;
                    n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                    logger.info("n_rep_id="+n_rep_id);
                }
            }
            else if (sqlpath.equals("rep_publish")){//публикуем отчет
                n_cp=820;
                logger.info("rep_publish");
                n_cp=900;
                new_params = new Object[2];
                int n_id = 0;
                n_cp++;
                new_params[n_id] = params.get("t_rep_stat");
                logger.info("t_rep_stat, new_params[" + n_id + "]="+new_params[n_id]);
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("id");
                logger.info("id, new_params[" + n_id + "]="+new_params[n_id]);
                KeyHolder kh = daoutil.updates("reports/rep_upd_status", new_params);
                n_cp++;
                n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                logger.info("n_rep_id="+n_rep_id);
                if (n_rep_id != 0) {
                    Object[] par_val;
                    String user_mail = "";
                    par_val = new Object[1];
                    par_val[0] = params.get("t_build");
                    if(par_val[0]!=null) {//если дом указан
                        List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_cit_mail_by_build", par_val);
                        for (int i = 0; i < s_res.size(); i++) {
                            if (i != s_res.size() - 1)
                                user_mail += s_res.get(i) + ",";
                            else
                                user_mail += s_res.get(i);
                        }
                        Map<String, String> par_send = new HashMap<>();
                        user_mail = user_mail.replace("{text=", "");
                        user_mail = user_mail.replace("}", "");
                        logger.info("user_mail = " + user_mail);
                        if (user_mail != null) {
                            Double repStat = (Double) params.get("t_rep_stat");
                            if (repStat == 2.0) {
                                par_send.put("MSG_CODE", "NEW_REP");
                            }
                            par_send.put("MSG_LANG", "RU");
                            par_send.put("REP_TYPE", (String) params.get("rep_type"));
                            Object[] values = new Object[2];
                            values[0] = 1;
                            values[1] = params.get("ksk_id");
                            String address = params.get("build_name").toString();
                            String kskName = daoutil.textByID("sprav/get_name_ksk_as_text", values);
                            par_send.put("REP_ADDRESS", address);
                            par_send.put("KSK_NAME", kskName);
                            par_send.put("ACCEPT_KSK", conf.getHost() + "/citreports.html");
                            par_send.put("EMAIL", user_mail);
                            userNotifService.sendOtherMessToAll(par_send);
                        }
                    }
                    else {//если выбраны все дома
                        logger.info("выбраны все дома");
                    }
                }
            }
            else if (sqlpath.equals("rep_doc_val")){
                n_cp=910;
                logger.info(sqlpath);
                String saction= (String) params.get("action");
                logger.info("saction=" + saction);
                if (saction.equals("ins")){
                    new_params = new Object[5];
                    int n_id = 0;
                    n_cp++;
                    new_params[n_id] = params.get("sdescription");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("ksk_id");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("ndoc_type");
                    n_id++;
                    n_cp++;
                    if (params.get("date_start")!=null){
                        new_params[n_id] = daoutil.StrtoDate(params.get("date_start").toString());
                        logger.info("n_cp=" + n_cp + ",n_id=" + n_id + " = " + new_params[n_id].toString());
                    }
                    n_id++;
                    n_cp++;
                    if (params.get("date_end")!=null){
                        new_params[n_id] = daoutil.StrtoDate(params.get("date_end").toString());
                    }
                }
                else {
                    n_cp=920;
                    new_params = new Object[3];
                    int n_id = 0;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("status");
                    n_id++;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("doc_value");
                    n_id++;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("doc_id");
                }
                //logger.info("new_params="+new_params.toString());
                KeyHolder kh = daoutil.updates("reports/"+sqlpath+"_"+saction, new_params);
                n_cp++;
                logger.info("n_cp="+n_cp);
                n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                logger.info("n_rep_id="+n_rep_id);
            }
            else if (sqlpath.equals("rep_doc_val_with_link")){
                n_cp=910;
                logger.info(sqlpath);
                String saction= (String) params.get("action");
                logger.info("saction=" + saction);
                if (saction.equals("ins")){
                    new_params = new Object[6];
                    int n_id = 0;
                    n_cp++;
                    new_params[n_id] = params.get("sdescription");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("ksk_id");
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("ndoc_type");
                    n_id++;
                    n_cp++;
                    if (params.get("date_start")!=null){
                        new_params[n_id] = daoutil.StrtoDate(params.get("date_start").toString());
                        logger.info("n_cp=" + n_cp + ",n_id=" + n_id + " = " + new_params[n_id].toString());
                    }
                    n_id++;
                    n_cp++;
                    if (params.get("date_end")!=null){
                        new_params[n_id] = daoutil.StrtoDate(params.get("date_end").toString());
                    }
                    n_id++;
                    n_cp++;
                    new_params[n_id] = params.get("link");

                }
                else {
                    n_cp=920;
                    new_params = new Object[3];
                    int n_id = 0;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("status");
                    n_id++;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("doc_value");
                    n_id++;
                    n_cp++;
                    logger.info("n_cp="+n_cp);
                    new_params[n_id] = params.get("doc_id");
                }
                //logger.info("new_params="+new_params.toString());
                KeyHolder kh = daoutil.updates("reports/"+sqlpath+"_"+saction, new_params);
                n_cp++;
                logger.info("n_cp="+n_cp);
                n_rep_id=Integer.valueOf(kh.getKeys().get("id").toString());
                logger.info("n_rep_id="+n_rep_id);
            }
            MDC.put("action", "rep_insert");
            logger.info("rep_insert end "+n_rep_id);
            return String.valueOf(n_rep_id);
        }
        catch (Exception e) {
            MDC.put("action", "rep_insert");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
    }

    @SpringTransactional
    public String votes(Map<String, Object> params) {
        MDC.put("action", "votes");
        int n_cp=1495;
        int n_vote_id=0, n_ques_id=0, n_ans_id=0, nid=0;
        Map<String, Object> rec_vote, rec_ques, cur_ques, cur_anskz, cur_ansru;
        try {
            logger.info("params="+params.toString());
            Object[] new_params;
            String sqlpath= (String) params.get("sqlpath");
            if (sqlpath.equals("vote_ins")){
                rec_vote = (Map<String, Object>) params.get("rec_vote");
                rec_ques = (Map<String, Object>) params.get("rec_ques");

                logger.info("rec_vote="+rec_vote.toString());
                logger.info("rec_ques="+rec_ques.toString());

                n_cp=1509;
                new_params = new Object[7];
                int n_id = 0;
                n_cp++;
                new_params[n_id] = rec_vote.get("descru");
                logger.info("descru="+(new_params[n_id].toString()));
                n_id++;
                n_cp++;
                new_params[n_id] = daoutil.StrtoDate(rec_vote.get("date_beg").toString());
                logger.info("date_beg="+(new_params[n_id].toString()));
                n_id++;
                n_cp++;
                new_params[n_id] = daoutil.StrtoDate(rec_vote.get("date_end").toString());
                logger.info("date_end="+(new_params[n_id].toString()));
                n_id++;
                n_cp++;
                if (params.get("t_ksk_id")!=null) {
                    new_params[n_id] = params.get("t_ksk_id");
                    logger.info("t_ksk_id="+(new_params[n_id].toString()));
                }
                n_id++;
                n_cp++;
                if (params.get("t_org_id")!=null) {
                    new_params[n_id] = params.get("t_org_id");
                    logger.info("t_org_id="+(new_params[n_id].toString()));
                }
                n_id++;
                n_cp++;
                new_params[n_id] = 1;   // status
                n_id++;
                n_cp++;
                new_params[n_id] = params.get("userId");
                logger.info("userId="+(new_params[n_id].toString()));
                KeyHolder kh = daoutil.updates("votes/"+sqlpath, new_params);
                n_cp++;
                n_vote_id=Integer.valueOf(kh.getKeys().get("id").toString());
                MDC.put("action", "votes");
                logger.info("n_vote_id="+n_vote_id);
                n_cp=1547;
                new_params = new Object[4];
                n_cp++;
                new_params[0] = n_vote_id;                // table_id
                n_cp++;
                new_params[1] = "t_vote";                 // table_name
                n_cp++;
                new_params[2] = 2;                        // t_language_id
                n_cp++;
                new_params[3] = rec_vote.get("desckz");   // lang_text
                kh = daoutil.updates("requests/insert_lang_text", new_params);

                new_params[2] = 1;                        // t_language_id
                new_params[3] = rec_vote.get("descru");   // lang_text
                kh = daoutil.updates("requests/insert_lang_text", new_params);

                n_cp=1563;
                for(Iterator<Map.Entry<String, Object>> ques_line = rec_ques.entrySet().iterator(); ques_line.hasNext(); ) {
                    Map.Entry<String, Object> entry = ques_line.next();
                    cur_ques = (Map<String, Object>) entry.getValue();
                    logger.info("cur_ques="+cur_ques.toString());
                    n_cp=1569;
                    new_params = new Object[2];
                    new_params[0] = n_vote_id;
                    n_cp=1571;
                    new_params[1] = Integer.valueOf(cur_ques.get("nradio").toString());                         // n_radio
                    kh = daoutil.updates("votes/vote_ques_ins", new_params);
                    n_ques_id=Integer.valueOf(kh.getKeys().get("id").toString());
                    logger.info("n_ques_id="+n_ques_id);
                    n_cp=1576;
                    new_params = new Object[4];
                    n_cp++;
                    new_params[0] = n_ques_id;                 // table_id
                    n_cp++;
                    new_params[1] = "t_vote_ques";             // table_name
                    n_cp++;
                    new_params[2] = 2;                         // t_language_id
                    n_cp++;
                    new_params[3] = cur_ques.get("inputskz");   // lang_text
                    kh = daoutil.updates("requests/insert_lang_text", new_params);
                    n_cp=1587;
                    new_params[2] = 1;                         // t_language_id
                    new_params[3] = cur_ques.get("inputsru");   // lang_text
                    kh = daoutil.updates("requests/insert_lang_text", new_params);
                    n_cp=1591;
                    cur_anskz = (Map<String, Object>) cur_ques.get("anskz");
                    logger.info("anskz="+cur_anskz.toString());
                    n_cp=1594;
                    cur_ansru = (Map<String, Object>) cur_ques.get("ansru");
                    logger.info("ansru="+cur_ansru.toString());
                    nid=0;
                    for(Iterator<Map.Entry<String, Object>> anskz_line = cur_anskz.entrySet().iterator(); anskz_line.hasNext(); ) {
                        n_cp=1597;
                        Map.Entry<String, Object> entrykz = anskz_line.next();
                        n_cp=1600;
                        logger.info("cur_val="+cur_ansru.get("val"+nid));
                        new_params = new Object[2];
                        new_params[0] = n_ques_id;                     // t_vote_ques_id
                        n_cp=1605;
                        new_params[1] = nid+1;                         // n_order
                        kh = daoutil.updates("votes/vote_ans_ins", new_params);
                        n_ans_id=Integer.valueOf(kh.getKeys().get("id").toString());
                        n_cp=1609;
                        new_params = new Object[4];
                        n_cp++;
                        new_params[0] = n_ans_id;                 // table_id
                        n_cp++;
                        new_params[1] = "t_vote_answ";             // table_name
                        n_cp++;
                        new_params[2] = 2;                         // t_language_id
                        n_cp++;
                        new_params[3] = cur_anskz.get("val"+nid);   // lang_text
                        kh = daoutil.updates("requests/insert_lang_text", new_params);
                        n_cp=1620;
                        new_params[2] = 1;                         // t_language_id
                        new_params[3] = cur_ansru.get("val"+nid);   // lang_text
                        kh = daoutil.updates("requests/insert_lang_text", new_params);
                        nid++;
                    }
                }
            }
            else if (sqlpath.equals("vote_send")) {
                n_cp = 1608;
                rec_vote = (Map<String, Object>) params.get("rec_vote");
                n_cp++;
                logger.info("rec_vote="+rec_vote.toString());
                n_cp++;
                rec_ques = (Map<String, Object>) params.get("receivers");
                n_cp++;
                logger.info("rec_ques="+rec_ques.toString());
                logger.info("rec_ques.size()="+rec_ques.size());
                //int n_vote_id=0, n_ques_id=0, n_ans_id=0, nid=0;
                n_vote_id= (int) Math.round((Double) rec_vote.get("recid"));
                logger.info("1640, n_vote_id="+n_vote_id);
                new_params = new Object[2];
                new_params[0] = n_vote_id;
                Map<String, String> par_send = new HashMap<>();

                KeyHolder kh;
                int n_cnt=0;
                for(Iterator<Map.Entry<String, Object>> receiv_line = rec_ques.entrySet().iterator(); receiv_line.hasNext(); ) {
                    n_cp=1612;
                    Map.Entry<String, Object> receivs = receiv_line.next();
                    //logger.info("1644, receivs.getKey="+receivs.getKey());
                    n_cp=1613;
                    nid= (int) Math.round((Double) receivs.getValue());
                    logger.info("1645, receivs.getValue="+nid);
                    n_cp=1614;
                    new_params[1] = nid;
                    kh = daoutil.updates("votes/vote_send_ins", new_params);
                    n_ques_id=Integer.valueOf(kh.getKeys().get("id").toString());
                    logger.info("vote_send_ins.id="+n_ques_id);

                    par_send.put("MSG_CODE","VOTE");
                    par_send.put("MSG_LANG","RU");
                    //logger.info("rec_vote.get(\"descr\")="+rec_vote.get("descr"));
                    par_send.put("COMMENTS",(String) rec_vote.get("descr"));
                    par_send.put("EMAIL", daoutil.textByID("sprav/user_email", (Double) rec_ques.get("uid"+n_cnt)));
                    //par_send.put("PUSH", "1");
                    //logger.info("EMAIL="+par_send.get("EMAIL"));
                    logger.info("par_send="+par_send.toString());
                    userNotifService.sendOtherMess(par_send);
                    n_cnt++;
                    receivs = receiv_line.next();

                }
                new_params[0] = 2;
                new_params[1] = n_vote_id;
                kh = daoutil.updates("votes/vote_upd", new_params);
                n_ques_id=Integer.valueOf(kh.getKeys().get("id").toString());
                logger.info("vote.id="+n_ques_id);
            }
            else if (sqlpath.equals("vote_upd")){
                n_cp=1608;
                logger.info(sqlpath);
            }
            else if (sqlpath.equals("vote_res_ins")){
                n_cp=1608;
                logger.info(sqlpath);
                logger.info("Sauran = "+params.get("vote_send_id").toString());
                new_params = new Object[3];
                new_params[0] = params.get("vote_send_id");
                new_params[1] = params.get("status");
                new_params[2] = params.get("ansIds");
                KeyHolder kh = daoutil.inserts("votes/"+sqlpath, new_params);
            }

            MDC.put("action", "votes");
            logger.info("votes end "+n_vote_id);
            return String.valueOf(n_vote_id);
        }
        catch (Exception e) {
            MDC.put("action", "votes");
            logger.error("n_cp = "+ n_cp + ", exception, name= " + e.getMessage() + ", stack="+e.getStackTrace());
            throw null;
        }
    }  //  end votes

}