package com.bas.auction.auth.controller;

  import com.bas.auction.auth.dto.User;
  import com.bas.auction.auth.service.AuthService;
  import com.bas.auction.auth.service.SpravService;
  import com.bas.auction.core.config.security.CurrentUser;
  import com.bas.auction.profile.employee.dto.RegUser;
  import com.bas.auction.profile.employee.service.impl.UserRegService;
  import org.apache.log4j.MDC;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  import java.io.IOException;
  import java.util.*;

  import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
  import static org.springframework.web.bind.annotation.RequestMethod.POST;
  import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(path = "/sprav", produces = APPLICATION_JSON_UTF8_VALUE)
public class SpravController {
    private  final static Logger logger = LoggerFactory.getLogger(SpravController.class);
    private final UserRegService userRegService;
    private final AuthService authService;
    private final SpravService spravService;

    @Autowired
    public SpravController(UserRegService userRegService, AuthService authService, SpravService spravService){
        this.userRegService=userRegService;
        this.authService = authService;
        this.spravService = spravService;
    }


    @RequestMapping(path="/main", method = POST)
    public List<Map<String, Object>> SpravReqMain (@RequestBody Map<String, String> params) throws  IOException {
        //logger.debug("Controller sprav/" + params.get("name"), params.get("lang_id"));
        return spravService.findSpravInfo(Integer.parseInt(params.get("lang_id")), params.get("name"));
    }


    @RequestMapping(path="/sub", method = POST)
    public List<Map<String, Object>> SpravReqSub (@RequestBody Map<String, String> params) throws  IOException {
        //logger.debug("Controller sprav/req_subtype ", params.get("lang_id"));
        return spravService.findSpravInfoChild(Integer.parseInt(params.get("lang_id")), params.get("name"), Integer.parseInt(params.get("sid")));
    }

    @RequestMapping(path="/other", method = POST)
    public List<Map<String, Object>> SpravOther (@RequestBody Map<String, Object> params,  @CurrentUser User user) throws  IOException {
        logger.info("sprav/other params = " + params.toString());
        logger.info("sprav/other count = " + params.size());
        List<Map<String, Object>> res;

        for(Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            logger.info("entry.getKey()="+entry.getKey());
            if(entry.getKey().equals("userId")) {
                //it.remove();
                logger.info("in if statement, getUserId="+user.getUserId());
                //params.put("userdsId", user.getUserId());
                params.put(entry.getKey(), user.getUserId());
            }
        }
        res=spravService.findOther(params);
        MDC.put("action","SpravOther");
        logger.info("res="+res.toString());
        return res;
    }

@RequestMapping(path="/subid", method = POST)
    public List<Map<String, Object>> SpravReqSubById (@RequestBody Map<String, String> params) throws  IOException {
        //logger.debug("Controller sprav/req_subtype ", params.get("lang_id"));
        return spravService.findSpravChildById( params.get("name"), Integer.parseInt(params.get("sid")));
    }


   @RequestMapping(path="/notifid", method = POST)
    public List<Map<String, Object>> SpravNotifById (@RequestBody Map<String, List<Map<String, Object>>> params) throws  IOException {

        logger.debug("Controller size{}{}"+params.get("ds").size());
        logger.debug("Controller sprav/req_subtype "+ params.get("ds").get(0).get("id"));

        String IdArray[] = new String[params.get("ds").size()];

        for (int i = 0; i<params.get("ds").size(); i++){
            logger.debug("Controller aaaeee{}{}"+(params.get("ds").get(0).get("id")));
          IdArray[i]=params.get("ds").get(i).get("id").toString();

        }

        logger.debug("Controller /sdsdsd"+IdArray.length);

        for (int j = 0; j<IdArray.length; j++){
            logger.debug("Controller /notifid"+IdArray[j]);
        }

	    return spravService.findSpravChildByIdArr((String)params.get("name").get(0).get("sql"),Integer.parseInt(params.get("name").get(0).get("lang_ids").toString().substring(0,1)), IdArray);

    }
}