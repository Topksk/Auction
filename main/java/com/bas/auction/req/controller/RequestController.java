package com.bas.auction.req.controller;

import com.bas.auction.auth.dao.SessionCreateDAO;
import com.bas.auction.auth.dao.UserDAO;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.SpravService;
import com.bas.auction.auth.service.UserNotificationService;
import com.bas.auction.auth.service.UserService;

import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.profile.employee.dto.RegUser;
import com.bas.auction.profile.employee.service.impl.UserRegService;
import com.bas.auction.req.draft.service.ReqDraftService;
import com.bas.auction.req.dto.Request;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
/*import java.sql.SQLException;*/
import java.util.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
@RequestMapping(path = "/reqs", produces = APPLICATION_JSON_UTF8_VALUE)
public class RequestController extends RestControllerExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(RequestController.class);
    private final ReqDraftService ReqDraftService;
    private final UserService userService;
    private final UserDAO userDAO;
	private final SpravService spravService;
    private final UserNotificationService userNotifService;
    private final Conf conf;
    private final DocFileDAO docFileDao;
    private final SessionCreateDAO sessionCreateDAO;
    private final UserRegService userRegService;
    private final DaoJdbcUtil daoutil;

    @Autowired
    public RequestController(MessageDAO messageDAO, ReqDraftService ReqDraftService, UserService userService, UserDAO userDAO, SpravService spravService, UserNotificationService userNotifService, Conf conf, DocFileDAO docFileDao, SessionCreateDAO sessionCreateDAO, UserRegService userRegService,DaoJdbcUtil daoutil) {
        super(messageDAO);
        this.ReqDraftService = ReqDraftService;
		this.userService = userService;
        this.userDAO = userDAO;
        this.spravService = spravService;
        this.userNotifService = userNotifService;
        this.conf=conf;
        this.docFileDao=docFileDao;
        this.sessionCreateDAO = sessionCreateDAO;
        this.userRegService = userRegService;
        this.daoutil = daoutil;
    }

	@ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/insreq", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String createCit(@RequestBody Map<String, Object> params,
                           @CurrentUser User user,Request req) {
        String sended=null;
        String err = null;
        String sqlpath=null;
        Boolean kskNotif=false;
        String host_ip = null;
        List<Map<String, Object>> kskEmail = null;
        List<Map<String, Object>> ksk = null;
        List<Map<String, Object>> rel_id=null;
        MDC.put("action", "createCit");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }
        for(Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            if(entry.getKey().equals("userId")) {
                params.put("userId", user.getUserId());
            }
            else if(entry.getKey().equals("userMail")) {
                params.put("userMail", user.getLogin());
            }
            else if(entry.getKey().equals("addressId")){
                kskNotif=true;
            }
            else if(entry.getKey().equals("sqlpath")){
                sqlpath=entry.getValue().toString();
            }else if(entry.getKey().equals("ip")){
                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    host_ip = ip.toString();
                    params.put("ip", host_ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                logger.debug("host_ip="+host_ip);
            };
        }
        logger.info("sqlpath="+sqlpath);
        if (sqlpath.equals("insert_ksk")){
            err=ReqDraftService.ins_ksk(params, user);
        }
        else if (sqlpath.substring(0,4).equals("cit_")){
            ReqDraftService.cit_relation(params, user);
        }
        else if (sqlpath.substring(0,4).equals("rep_")){
            err=ReqDraftService.rep_insert(params, user);
            MDC.put("action", "createCit");
            logger.info("sqlpath="+ sqlpath + ", err="+err);
        }
        else if (sqlpath.equals("insert_guest_feeedback")){
            err= ReqDraftService.creates(params, user);
            Map<String, String> par_send = new HashMap<>();
            par_send.put("MSG_LANG","RU");
            par_send.put("MSG_CODE","FEEDBACK");
            par_send.put("COMMENTS",(String) params.get("comments"));
            par_send.put("PHONENUM",(String) params.get("phonenum"));
            par_send.put("GUESTEMAIL",(String) params.get("guestemail"));
            par_send.put("FLNAME",(String) params.get("flname"));

            //par_send.put("EMAIL","g.omarov@cloudmaker.kz");
            //userNotifService.sendOtherMess(par_send);
            par_send.put("EMAIL","dev@cloudmaker.kz");
            userNotifService.sendOtherMess(par_send);
            par_send.put("EMAIL","sales@cloudmaker.kz");
            userNotifService.sendOtherMess(par_send);
            par_send.put("EMAIL",(String) params.get("guestemail"));
            userNotifService.sendOtherMess(par_send);
        }
        else {
            err= ReqDraftService.creates(params, user);
            logger.info("RequestController Edit Address");
            if (kskNotif) {
                logger.info("err.indexOf(exceptio)="+err.indexOf("exception"));
                if(err.indexOf("exception")==-1) {
                    rel_id = userService.getRelIdByUserParams(params);
                    logger.info("rel_id return value size==="+rel_id.size());
                }

                if(err.indexOf("exception")==-1) {
                    kskEmail = userService.getEmployeeByRelId((Long)rel_id.get(0).get("id"));
                    logger.info("kskEmail===="+kskEmail);
                }

              /*  if ((kskEmail==null || kskEmail.isEmpty()) && (err.indexOf("exception")==-1)){
                    logger.info("kskEmail is  empty");
                    kskEmail = userService.getEmployeeEmail2(user.getUserId());
                }*/
                if(kskEmail!=null && !kskEmail.isEmpty() ) {
                    for (int i = 0; i < kskEmail.size(); i++) {
                        userService.sendUserRegNotifToKSK((String) kskEmail.get(i).get("usermail1"), userService.getSupplAddress(user.getUserId(), (Long)rel_id.get(0).get("id") ), (userDAO.getUserName(user.getUserId())));
                    }
                }

                ksk = userService.getKskByUserParams(params);

               if ((ksk!=null && !ksk.isEmpty()) && err.indexOf("exception")==-1){
                    userDAO.updateRelation(user.getUserId());
                }else{
                   err+="KSK_NOT_FOUND";
                }
            }
            else{
                logger.info("kskNotif=false");
            }
        }
        return err;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/execFunc", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String execFunc(@RequestBody Map<String, Object> params,
                            @CurrentUser User user,Request req) {
        String sended=null;
        String err = null;
        String sqlpath=null;
        Boolean kskNotif=false;
        String host_ip = null;
        List<Map<String, Object>> rel_id=null;
        List<Map<String, Object>> kskEmail = null;
        List<Map<String, Object>> ksk;
        List<Map<String, Object>> res;
        MDC.put("action", "createCit");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        for(Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            if(entry.getKey().equals("userId")) {
                params.put("userId", user.getUserId());
            }
            else if(entry.getKey().equals("userMail")) {
                params.put("userMail", user.getLogin());
            }
            else if(entry.getKey().equals("addressId")){
                kskNotif=true;
            }
            else if(entry.getKey().equals("sqlpath")){
                sqlpath=entry.getValue().toString();
            }else if(entry.getKey().equals("ip")){
                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    host_ip = ip.toString();
                    params.put("ip", host_ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                logger.debug("host_ip="+host_ip);
            };
        }

        if (params.get("sqlpath").equals("f_serv_req_upds")
                &&!params.get("status_serv_req").toString().equals("3.0")
                &&!params.get("status_serv_req").toString().equals("7.0")
                &&!params.get("status_serv_req").toString().equals("6.0")) {
            Map<String, Object> par_val = new HashMap<>();
            par_val.put("id_serv_req",params.get("id_serv_req"));
            par_val.put("userId",params.get("userId"));
            par_val.put("sqlpath","sprav/check_user");
            res=spravService.findOther(par_val);
            logger.info("count_pos = "+res.get(0).get("count_pos").toString());
            if (Integer.parseInt(res.get(0).get("count_pos").toString())>0) {
            err= ReqDraftService.execFunc(params, user);
            }
        }
        else {
            err= ReqDraftService.execFunc(params, user);
        }

            logger.info("ReqDraftService.err="+err);
            if (kskNotif) {

                if(err.indexOf("exception")==-1) {
                    rel_id = userService.getRelIdByUserParams(params);
                    logger.info("rel_id return value size==="+rel_id.size());
                }

                if(err.indexOf("exception")==-1) {
                    kskEmail = userService.getEmployeeByRelId((Long)rel_id.get(0).get("id"));
                    logger.info("kskEmail===="+kskEmail);
                }

                ksk = userService.getKskByUserParams(params);

                if(kskEmail!=null && !kskEmail.isEmpty() ) {
                    for (int i = 0; i < kskEmail.size(); i++) {
                        userService.sendUserRegNotifToKSK((String) kskEmail.get(i).get("usermail1"), userService.getSupplAddress(user.getUserId(), (Long)rel_id.get(0).get("id")), (userDAO.getUserName(user.getUserId())));
                    }
                }

                if (!ksk.isEmpty()){
                    userDAO.updateRelation(user.getUserId());
                }else{
                    err+="KSK_NOT_FOUND";
                }
            }
            else{
                logger.info("kskNotif=false");
            }

        return err;
    }


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/execFunc3", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String execFunc3(@RequestBody Map<String, Object> params,
                           @CurrentUser User user,Request req) {
        String sended=null;
        String err = null;
        String sqlpath=null;
        Boolean kskNotif=false;
        String host_ip = null;
        List<Map<String, Object>> rel_id=null;
        List<Map<String, Object>> kskEmail = null;
        List<Map<String, Object>> ksk;
        List<Map<String, Object>> res;
        MDC.put("action", "createCit");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        for(Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            if(entry.getKey().equals("userId")) {
                params.put("userId", user.getUserId());
            }
            else if(entry.getKey().equals("userMail")) {
                params.put("userMail", user.getLogin());
            }
            else if(entry.getKey().equals("addressId")){
                kskNotif=true;
            }
            else if(entry.getKey().equals("sqlpath")){
                sqlpath=entry.getValue().toString();
            }else if(entry.getKey().equals("ip")){
                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    host_ip = ip.toString();
                    params.put("ip", host_ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                logger.debug("host_ip="+host_ip);
            };
        }

        if (params.get("sqlpath").equals("f_serv_req_upds")
                &&!params.get("status_serv_req").toString().equals("3.0")
                &&!params.get("status_serv_req").toString().equals("7.0")
                &&!params.get("status_serv_req").toString().equals("6.0")) {
            Map<String, Object> par_val = new HashMap<>();
            par_val.put("id_serv_req",params.get("id_serv_req"));
            par_val.put("userId",params.get("userId"));
            par_val.put("sqlpath","sprav/check_user");
            res=spravService.findOther(par_val);
            logger.info("count_pos = "+res.get(0).get("count_pos").toString());
            if (Integer.parseInt(res.get(0).get("count_pos").toString())>0) {
                err= ReqDraftService.execFunc3(params, user);
            }
        }
        else {
            err= ReqDraftService.execFunc3(params, user);
        }

        logger.info("ReqDraftService.err="+err);
        if (kskNotif) {

            if(err.indexOf("exception")==-1) {
                rel_id = userService.getRelIdByUserParams(params);
                logger.info("rel_id return value size==="+rel_id.size());
            }

            if(err.indexOf("exception")==-1) {
                kskEmail = userService.getEmployeeByRelId((Long)rel_id.get(0).get("id"));
                logger.info("kskEmail===="+kskEmail);
            }

            ksk = userService.getKskByUserParams(params);

            if(kskEmail!=null && !kskEmail.isEmpty() ) {
                for (int i = 0; i < kskEmail.size(); i++) {
                    userService.sendUserRegNotifToKSK((String) kskEmail.get(i).get("usermail1"), userService.getSupplAddress(user.getUserId(), (Long)rel_id.get(0).get("id")), (userDAO.getUserName(user.getUserId())));
                }
            }

            if (!ksk.isEmpty()){
                userDAO.updateRelation(user.getUserId());
            }else{
                err+="KSK_NOT_FOUND";
            }
        }
        else{
            logger.info("kskNotif=false");
        }

        return err;
    }


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/actv", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String activateAddress(@RequestBody Map<String, Object> params,
                                @CurrentUser User user,Request req) {
        List<Map<String, Object>>  kskEmail = null;
        String err = null;
        MDC.put("action", "activateAddress");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        logger.debug("activateAddress{}{}{}{}"+  (Long) Math.round((Double) params.get("rel_id")));
        try {
            kskEmail = userService.getKskEmailAddress(user.getUserId(), params.get("rel_id"));
            for (int i = 0; i<kskEmail.size(); i++){
                userService.sendUserRegNotifToKSK((String) kskEmail.get(i).get("usermail1"), userService.getSupplAddress(user.getUserId(), (Long) Math.round((Double) params.get("rel_id"))), (userDAO.getUserName(user.getUserId())));
            }

            if (kskEmail.isEmpty()) {
                err="KSK_NOT_FOUND";
            }

                userDAO.activateRelation(user.getUserId(), params.get("status") , params.get("status1") , params.get("rel_id"),(String)params.get("sqlpath"));

        } catch (Exception e) {
        }
        return err;
    }


  @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/uploadImage3",method = RequestMethod.POST)
    public @ResponseBody String uploadImage3(@RequestBody List<List<Map<String, Object>>> params,
                                             @CurrentUser User user,
                                             HttpServletRequest request) throws IOException {
        MDC.put("action", "uploadImage3");


        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        FileOutputStream outFile = null;
        String ftype="", inType;
        Double stab_id;
        String table_name="";
        Long table_id;
        Long file_id[]=new Long[2];
        Map<String, Object> params2 =  new HashMap<String, Object>();
        List<Map<String, Object>> checkRequest;

        try
        {




            if(params.get(0).get(0).get("bindReq")!=null) {
                checkRequest = spravService.findSpravInfo(Integer.parseInt((String) params.get(0).get(0).get("bindReq")), "chech_request_id");
                if (checkRequest.size() == 0) {
                    return "request_id_notFound";
                }
            }

                //logger.info("stab_id=" + table_id.toString());
                table_name = (String) params.get(0).get(0).get("table_name");

                logger.info("table_name=" + table_name);
                for (int i = 0; i < params.size(); i++) {
                    byte[] imageByte = Base64.decodeBase64((String) params.get(i).get(0).get("content"));
                    inType = (String) params.get(i).get(0).get("type");
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
                    logger.info("Type defined{}=" + ftype);
                    DocFile docFile = new DocFile();
                    docFile.setCreatedBy(user.getUserId());
                    docFile.setFileName((String) params.get(i).get(0).get("name"));
                    docFile.setFileType((String) params.get(i).get(0).get("type"));
                    docFile.setFileSize(Math.round((Double) params.get(i).get(0).get("size")));
                    docFile.setPath(conf.getFileStorePath());
                    docFile.setHashValue((String) params.get(i).get(0).get("content"));
                    docFile.setIsSystemGenerated(false);
                /*docFile.setTableId(table_id);*/
                    docFile.setTableName(table_name);
                    if (!ftype.isEmpty()) {
                        MDC.put("action", "create");
                        logger.info("docFile=" + docFile.toString());
                        Long fileId = docFileDao.create(docFile);
                        file_id[i] = fileId;
                        logger.info("fileId{}=" + fileId);
                        String directory = docFile.getPath() + fileId + ftype;
                        File f = new File(directory);
                        outFile = new FileOutputStream(f);
                        outFile.write(imageByte);
                        ftype = "";
                    }
                    outFile.close();
                }

                params2.put("userId", user.getUserId());
                params2.put("ksk_id", params.get(0).get(0).get("kskid"));
                params2.put("t_req_id", params.get(0).get(0).get("bindReq"));
                params2.put("before_f_id", file_id[0]);
                params2.put("after_f_id", file_id[1]);
                params2.put("comment", params.get(0).get(0).get("comment"));
                params2.put("sqlpath", "f_insert_t_ksk_gallery");
                logger.info("paramsasd=" + params2.toString());

                String err = ReqDraftService.execFunc(params2, user);

                if (err.equals("0")) {
                    logger.info("sadsadasdsadsadsad");
                    return "success";
                } else {
                    return err;
                }


        }
        catch(Exception e)
        {
            MDC.put("action", "uploadImage3");
            logger.info(e.getMessage());
            logger.info("error = "+e);
            throw null;
        }
    }




    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/uploadImage2",method = RequestMethod.POST)
    public @ResponseBody String uploadImage2(@RequestBody List<Map<String, Object>> params,
                                             @CurrentUser User user,
                                             HttpServletRequest request) throws IOException {
        MDC.put("action", "uploadImage2");
        logger.info("params=" + params.toString());

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        FileOutputStream outFile = null;
        String ftype="", inType;
        Double stab_id;
        String table_name="";
        Long table_id;
        try
        {
            logger.info("getSimpleName = "+params.get(0).get("tableId").getClass().getSimpleName());
            if (params.get(0).get("tableId").getClass().getSimpleName().equals("Double")) {
                stab_id = (Double) params.get(0).get("tableId");
                table_id=Math.round(stab_id);
                //logger.info("double");
                //equals("class java.lang.Double")
            }
            else {
                //logger.info("string");
                table_id=(Long.parseLong(params.get(0).get("tableId").toString()));
            }
            //logger.info("stab_id=" + table_id.toString());
            table_name=(String) params.get(0).get("table_name");
            logger.info("table_id=" + table_id.toString());
            logger.info("table_name=" + table_name);
            for (int i = 0; i<params.size(); i++){
                byte[] imageByte= Base64.decodeBase64((String) params.get(i).get("content"));
                inType=(String) params.get(i).get("type");
                if (inType.equals("image/png")){
                    ftype=".png";
                }else if (inType.equals("image/jpeg")) {
                    ftype = ".jpg";
                }else if (inType.equals("image/jpg")){
                    ftype=".jpg";
                }else if (inType.equals("image/gif")){
                    ftype=".gif";
                }else if (inType.equals("image/tiff")){
                    ftype=".tiff";
                }else if (inType.equals("image/psd")){
                    ftype=".psd";
                }else if (inType.equals("application/pdf")){
                    ftype=".pdf";
                }
                logger.info("Type defined{}="+ftype);
                DocFile docFile = new DocFile();
                docFile.setCreatedBy(user.getUserId());
                docFile.setFileName((String) params.get(i).get("name"));
                docFile.setFileType((String) params.get(i).get("type"));
                docFile.setFileSize(Math.round((Double) params.get(i).get("size")) );
                docFile.setPath(conf.getFileStorePath());
                docFile.setHashValue((String) params.get(i).get("content"));
                docFile.setIsSystemGenerated(false);
                docFile.setTableId(table_id);
                docFile.setTableName(table_name);
                if(!ftype.isEmpty()) {
                    MDC.put("action", "create");
                    logger.info("docFile="+docFile.toString());
                    Long fileId = docFileDao.create(docFile);
                    logger.info("fileId{}=" + fileId);
                    String directory = docFile.getPath() + fileId + ftype;
                    File f = new File(directory);
                    outFile = new FileOutputStream(f);
                    outFile.write(imageByte);
                    ftype="";
                }
                outFile.close();
            }
            return "success ";
        }
        catch(Exception e)
        {
            MDC.put("action", "uploadImage2");
            logger.info(e.getMessage());
            logger.info("error = "+e);
            throw null;
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/uploadImageNotif",method = RequestMethod.POST)
    public @ResponseBody String uploadImageNotif(@RequestBody List<Map<String, Object>> params,
                                             @CurrentUser User user,
                                             HttpServletRequest request) throws IOException {


        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        FileOutputStream outFile = null;
        String ftype="", inType;
        try
        {

            for (int i = 0; i<params.size(); i++){
                byte[] imageByte= Base64.decodeBase64((String) params.get(i).get("content"));
                inType=(String) params.get(i).get("type");
                if (inType.equals("image/png")){
                    ftype=".png";
                }else if (inType.equals("image/jpeg")) {
                    ftype = ".jpg";
                }else if (inType.equals("image/jpg")){
                    ftype=".jpg";
                }else if (inType.equals("image/gif")){
                    ftype=".gif";
                }else if (inType.equals("image/tiff")){
                    ftype=".tiff";
                }else if (inType.equals("image/psd")){
                    ftype=".psd";
                }else if (inType.equals("application/pdf")){
                    ftype=".pdf";
                }
                logger.info("Type defined{}=");
                DocFile docFile = new DocFile();
                docFile.setCreatedBy(user.getUserId());
                docFile.setFileName((String) params.get(i).get("name"));
                docFile.setFileType((String) params.get(i).get("type"));
                docFile.setFileSize(Math.round((Double) params.get(i).get("size")) );
                docFile.setPath(conf.getFileStorePath());
                docFile.setHashValue((String) params.get(i).get("content"));
                docFile.setIsSystemGenerated(false);
                if(!ftype.isEmpty()) {
                    Long fileId = docFileDao.createNotifFile(docFile);
                    logger.info("fileId{}=" + fileId);

                    String directory = docFile.getPath() + fileId + ftype;
                    File f = new File(directory);
                    outFile = new FileOutputStream(f);
                    outFile.write(imageByte);
                    ftype="";
                }
                outFile.close();
            }
         /*   int i=0;
            //This will decode the String which is encoded by using Base64 class
            logger.info("params.get(imageValue)"+params.get("imageValue"));
            for (Map.Entry<String, String> entry : params.entrySet()){
                byte[] imageByte= Base64.decodeBase64(entry.getValue());
                String directory="C:/tmp/images/sample"+i+".jpg";
                new FileOutputStream(directory).write(imageByte);
                i++;
            }*/

            return "success ";
        }
        catch(Exception e)
        {
            logger.info("error = "+e);
            throw null;
        }

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/votes", method = RequestMethod.POST, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String votes(@RequestBody Map<String, Object> params,
                               @CurrentUser User user) {
        String err = null;
        MDC.put("action", "votes");
        logger.info("params="+params.toString());

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        for(Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            if(entry.getKey().equals("userId")) {
                params.put("userId", user.getUserId());
            };
        }

        /*if (params.get("sqlpath").equals("vote_ins")) {
            err= ReqDraftService.votes(params);
        }
        else {
            err= ReqDraftService.votes(params);
        }*/
        err= ReqDraftService.votes(params);

        logger.info("RequestController.err="+err);
        return err;
    }


    // Создание истории заявок
    @RequestMapping(path="/history", method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public String create_hist(@RequestBody Map<String, Object> params,
                               @CurrentUser User user) {
        MDC.put("action", "create_hist");

        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        logger.info("RequestController.create_hist, params=" + params.toString());
        String sqlpath = params.get("sqlpath").toString();
        logger.info("RequestController.create_hist, sqlpath=" + sqlpath);
        return ReqDraftService.req_hist(params, user);
    }

    @RequestMapping(path="/notification", method = POST)
    public void userNotification (@RequestBody Map<String, List<Map<String, Object>>> params,  @CurrentUser User user) throws IOException {
        if (params.get("name").get(0).get("sql").toString().equals("rate_notif_for_mod")) {
            Map<String, Object> params2=new HashMap<String, Object>();
            params2.put("sqlpath", "sprav/get_all_comp_moderatormail");
            String service=daoutil.textByID("sprav/req_type_by_subtype",params.get("name").get(0).get("serv_id"));
            List<Map<String, Object>> s_res1 = daoutil.queryForMapList(params2);
            if (s_res1.size() != 0) {
                String mailCheck = "", mailCheck1 = "";
                for (int i = 0; i < s_res1.size(); i++) {
                    mailCheck = s_res1.get(i).get("email").toString();
                    if (Validator.isValidEmail(mailCheck)) {
                        mailCheck1 = mailCheck1 + ", " + mailCheck;
                    }
                }
                Map<String, String> par_send = new HashMap<>();
                par_send.put("MSG_CODE", "RATE_NOTIF_FOR_MOD");
                par_send.put("MSG_LANG", "RU");
                par_send.put("EMAIL", mailCheck1);
                par_send.put("SERV", service);
                userNotifService.sendOtherMessToAll(par_send);
            }
        }
        else {
        if (  user!=null) {
            sessionCreateDAO.set_var(user.getUserId());
        }

        List<Map<String, Object>> userEmails;
        logger.debug("Controller size{}{}"+params.get("ds").size());
        logger.debug("Controller sprav/req_subtype "+ params.get("ds").get(0).get("id"));

        String IdArray[] = new String[params.get("ds").size()];
        for (int i = 0; i<params.get("ds").size(); i++){
            IdArray[i]=params.get("ds").get(i).get("id").toString();

        }
         userEmails= spravService.findSpravChildByIdArr((String)params.get("name").get(0).get("sql"),-1, IdArray);


        String [] emails=new String[userEmails.size()];
        for (int j = 0; j<userEmails.size(); j++){
            logger.debug("Controller /emails{}"+userEmails.get(j).get("text"));
            emails[j]=userEmails.get(j).get("text").toString();
        }

        userNotifService.sendNotification(emails, (String)params.get("name").get(0).get("cr_theme"), (String)params.get("name").get(0).get("cr_note"));
        }



    }

    @RequestMapping(path="/regserv", method= POST)
    public String servRegistration(@RequestBody List<Map<String, Object[]>> ds, @CurrentUser User user){
        RegUser ruser=new RegUser();
        Map<String, Object> params = new HashMap<String, Object>();
        String sqlpath=null, err = null;
        logger.info("servRegistration[]"+ds.get(0).get("ds")[0].toString());

        try {
            if(ds.get(0).get("sqlpath")[0].toString().equals("f_reg_supplier_service")) {
        ruser.setName(ds.get(0).get("cinfo")[0].toString());
        ruser.setSurname(ds.get(0).get("cinfo")[1].toString());
        ruser.setEmail(ds.get(0).get("cinfo")[2].toString());
        ruser.setMobilephone(ds.get(0).get("cinfo")[3].toString());
        ruser.setPassword(ds.get(0).get("cinfo")[4].toString());
            User u = null;
            if (ds.get(0).get("useCurInfo")[0].toString().equals("0")) {
                 u = userRegService.selfRegister(ruser);
                } else if (ds.get(0).get("useCurInfo")[0].toString().equals("1")) {
                //Старые данные сохраняются
                    Long userId = userDAO.findUserIdByEmail(ds.get(0).get("cinfo")[2].toString());
                    u = userDAO.findById(userId);
                try {
                    userService.checkPasswordByEmail(ds.get(0).get("cinfo")[2].toString(), ds.get(0).get("cinfo")[4].toString());
                } catch (Exception e) {
                    return "invalid_password";
                }
                } else if (ds.get(0).get("useCurInfo")[0].toString().equals("2")) {
                //новый данныйларды жазады логин пароль сакталады
                    logger.info("userInfodsdsd==" + ds.get(0).get("cinfo")[2].toString());
                    Long userId = userDAO.findUserIdByEmail(ds.get(0).get("cinfo")[2].toString());
                    logger.info("userId3424==" + ds.get(0).get("cinfo")[3].toString());
                    User usr = userDAO.findById(userId);
                try {
                    userService.checkPasswordByEmail(ds.get(0).get("cinfo")[2].toString(), ds.get(0).get("cinfo")[4].toString());
                } catch (Exception e) {
                    return "invalid_password";
                }

                usr.setName(ds.get(0).get("cinfo")[0].toString());
                usr.setSurname(ds.get(0).get("cinfo")[1].toString());
                usr.setMobilePhone(ds.get(0).get("cinfo")[3].toString());
                    u = userService.update(usr, usr);
            }


                params.put("classify", Arrays.toString(ds.get(0).get("ds")).substring(1, Arrays.toString(ds.get(0).get("ds")).length() - 1));
            params.put("addquest", ds.get(0).get("addquest")[0].toString());
                params.put("cinfo", Arrays.toString(ds.get(0).get("cinfo")).substring(1, Arrays.toString(ds.get(0).get("cinfo")).length() - 1));
                params.put("selectedCities", Arrays.toString(ds.get(0).get("selectedCities")).substring(1, Arrays.toString(ds.get(0).get("selectedCities")).length() - 1));
            params.put("otherServiceVal", ds.get(0).get("otherServiceVal")[0].toString());
                params.put("address", Arrays.toString(ds.get(0).get("address")).substring(1, Arrays.toString(ds.get(0).get("address")).length() - 1));
            params.put("userid", u.getUserId());
                logger.info("u.getEmail()=" + u.getEmail());
            params.put("email", u.getEmail());
                params.put("sqlpath", ds.get(0).get("sqlpath")[0].toString());
                err = ReqDraftService.execFunc(params, u);
            }else {

                params.put("classify", Arrays.toString(ds.get(0).get("ds")).substring(1, Arrays.toString(ds.get(0).get("ds")).length() - 1));
                params.put("addquest", ds.get(0).get("addquest")[0].toString());
                params.put("selectedCities", Arrays.toString(ds.get(0).get("selectedCities")).substring(1, Arrays.toString(ds.get(0).get("selectedCities")).length() - 1));
                params.put("otherServiceVal", ds.get(0).get("otherServiceVal")[0].toString());
                params.put("compid",  ds.get(0).get("compid")[0]);
                params.put("userid", user.getUserId());
                params.put("lang_id", ds.get(0).get("lang_id")[0]);
                params.put("sqlpath", ds.get(0).get("sqlpath")[0].toString());
                err = ReqDraftService.execFunc(params, user);

            }


        }catch (Exception e){
            logger.info("e.toString()= "+e.toString());
            if(e.toString().indexOf("ShortPasswordException")>0){
                return "ShortPasswordException";
            }else if (e.toString().indexOf("c_user_uq2")>0){
                return "AlreadyExistsEmail";
            }
            return err;
        }

               /* for (int i=0; i<ds.get(0).get("ds").length; i++) {
                    logger.info("ds" + i + "=" + (ds.get(0).get("ds")[i]));
                }
                for(int j=0; j<ds.get(1).get("addquest").length; j++) {
                    logger.info("ss" + j + "=" + ds.get(1).get("addquest")[j].toString());
                }
                for(int t=0; t<ds.get(2).get("cinfo").length; t++) {
                    logger.info("ss" + t + "=" + ds.get(2).get("cinfo")[t].toString());
                }*/
        return err;

    }

    @RequestMapping(path="/push", method = POST)

    public String testPush(@RequestBody Map<String, Object> params) {
        logger.info("Push_test");
        logger.info("Push_test, params="+params.toString());

        String s_err;
        String s_email;
        String s_text;

        logger.info("Push_test, params="+params.get("email").toString());
        s_email = params.get("email").toString();
        s_text = params.get("text").toString();

        int n_cp=500;

        s_err=userNotifService.sendPush(s_email, s_text);

        return s_err;
    }

    @RequestMapping(path="/delFiles", method= POST)
    public void deleteFiles(@RequestBody Map<String, Object> params, @CurrentUser User user) throws IOException{
        Long fileId = Long.parseLong(params.get("f_id").toString());
        docFileDao.delete(user, fileId);
    }
}