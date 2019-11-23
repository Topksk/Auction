package com.bas.auction.req.dao;


import com.bas.auction.auth.dto.User;
//import com.bas.auction.req.dto.Request;
import com.bas.auction.req.dto.ReqHistory;

import java.util.Date;
import java.util.Map;

public interface RequestDAO {
    void sendToExec(String execMail, Long n_req_id, String req_address, String req_type, Date dead_line);
    String inserts(Map<String, Object> params, User user);
    String execFunction(Map<String, Object> params, User user);
    String execFunction3(Map<String, Object> params, User user);
    String updates(Map<Integer, Object> params, Boolean many);
//    Request insert(Request req);
    String reqHist(ReqHistory reqHist, String sqlPath);
}
