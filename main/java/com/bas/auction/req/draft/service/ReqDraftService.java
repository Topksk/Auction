package com.bas.auction.req.draft.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.req.dto.ReqHistory;
import com.bas.auction.req.dto.Request;
import java.util.Map;
public interface ReqDraftService {
String creates(Map<String, Object> params, User user);
String execFunc(Map<String, Object> params, User user);
String execFunc3(Map<String, Object> params, User user);
String req_hist(Map<String, Object> params, User user);
String ins_ksk(Map<String, Object> params, User user);
void cit_relation(Map<String, Object> params, User user);
String rep_insert(Map<String, Object> params, User user);
String votes(Map<String, Object> params);
}