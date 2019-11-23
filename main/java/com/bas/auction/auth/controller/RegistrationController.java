package com.bas.auction.auth.controller;

import com.bas.auction.auth.dao.AuthDAO;
import com.bas.auction.auth.dao.SessionCreateDAO;
import com.bas.auction.auth.dto.User;
  import com.bas.auction.auth.service.AuthService;
  import com.bas.auction.auth.service.SpravService;
  import com.bas.auction.profile.employee.dto.RegUser;
  import com.bas.auction.profile.employee.service.impl.UserRegService;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
  import java.io.IOException;
  import java.util.List;
  import java.util.Map;
  import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
  import static org.springframework.web.bind.annotation.RequestMethod.POST;
  import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(path = "/regist", produces = APPLICATION_JSON_UTF8_VALUE)
public class RegistrationController {
    private  final static Logger logger = LoggerFactory.getLogger(RegistrationController.class);
    private final UserRegService userRegService;
    private final AuthService authService;
    private final SpravService spravService;
    private final AuthDAO authDAO;
    private final SessionCreateDAO sessionCreateDAO;

    @Autowired
    public RegistrationController(UserRegService userRegService, AuthService authService, SpravService spravService, AuthDAO authDAO, SessionCreateDAO sessionCreateDAO){
        this.userRegService=userRegService;
        this.authService = authService;
        this.spravService = spravService;
        this.authDAO = authDAO;
        this.sessionCreateDAO = sessionCreateDAO;
    }


    @RequestMapping(path="/register_user", method = PUT)
    public String RegisterUser (@RequestBody RegUser ruser,
                              HttpServletRequest request,
                              HttpServletResponse response) throws  IOException {
        logger.debug("Home Param={}"+ruser.getHome());
        User u = userRegService.selfRegister(ruser);
        logger.debug("u.getSntrue()={}"+u.getSntrue());
        logger.debug("u.getUserId()={}"+u.getUserId());

        Long userId=u.getUserId();

        if(userId==null){
            userId=authDAO.findUserIdByEmail(u.getEmail());
        }

        if (u.getSntrue()!=null){
            //** For AutoLogin after registration
            sessionCreateDAO.createSession(userId,request, response );
        }

        String additional = authDAO.findUserRel(u.getEmail());
        if (additional.equals("noteexists")) {
            return "/";
        }else{
            String additional2 = authDAO.findAuthMain(u.getEmail());

            if (additional2.substring(0,1).equals("2")) {
                return "login.html#REQUESTPOPUP";
            }else {
            return "notifforuser.html";
        }
    }
    }

    @RequestMapping(path="/city", method = POST)
    public List<Map<String, Object>> RegisterCity (@RequestBody Map<String, String> params) throws  IOException {
        logger.debug("Integer.parseInt(params.get(lang_id))"+Integer.parseInt(params.get("lang_id"))+" params.get(name)"+params.get("name"));
        return spravService.findSpravInfo(Integer.parseInt(params.get("lang_id")), params.get("name"));
    }


    @RequestMapping(path="/street", method = POST)
    public List<Map<String, Object>> RegisterStreet (@RequestBody Map<String, String> params) throws  IOException {
        logger.debug("sddsddddddd{}", params.get("id"));
        return spravService.findSpravInfoChild(Integer.parseInt(params.get("lang_id")), params.get("name"), Integer.parseInt(params.get("sid")));
    }


}
