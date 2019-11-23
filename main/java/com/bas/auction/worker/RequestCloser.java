package com.bas.auction.worker;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.UserService;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.profile.supplier.service.SupplierService;
import com.bas.auction.req.draft.service.ReqDraftService;
import com.bas.auction.core.Conf;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Component
public class RequestCloser implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(RequestCloser.class);
	private final DaoJdbcUtil daoutil;
	private final ReqDraftService reqservice;
	private final UserService userService;
	private final Conf conf;
	private final HttpServletRequest httpServReq;
	private final SupplierService supplierService;

	@Autowired
	public RequestCloser(DaoJdbcUtil daoutil, ReqDraftService reqservice, UserService userService, Conf conf, HttpServletRequest httpServReq, SupplierService supplierService) {
		this.daoutil = daoutil;
		this.reqservice = reqservice;
		this.userService = userService;
		this.conf = conf;
		this.httpServReq = httpServReq;
		this.supplierService = supplierService;
	}

	@Override
	//@Scheduled(/*initialDelay = 120000, fixedDelay = 3600000,*/ fixedRate = 3600000) // run every minute
	@Scheduled(cron = "0 00 * * * ?") // run every hour in 00 minute
	public void run() {
		MDC.clear();
		MDC.put("action", "RequestCloser");
		Thread.currentThread().setName("RequestCloser");
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> res;
		int n_cp=0;

		Date d_oper = new Date();
		Date d_stat = new Date();
		Calendar d_new = new GregorianCalendar();
		//Map<String, Object> par_other = new HashMap<>();
		//Map<Integer, Object> par_req = new TreeMap<Integer, Object>();

		logger.info("d_oper="+d_oper.toLocaleString());
		//String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr();

		//logger.info("d_new="+d_new.toString());
		d_new.add(Calendar.HOUR_OF_DAY, -48);
		d_oper=d_new.getTime();
		//dead_line.toLocaleString().substring(0,10)
		logger.info("d_oper2="+d_oper.toLocaleString());
		User user;
		user=userService.findById(Long.valueOf(1));
		logger.info("user.getLogin()="+user.getLogin());
		try {
			int nk=0;
			String s_id;
			n_cp=40;
			params.put("sqlpath", "sprav/reqs_list_by_status");
			params.put("id1", 4);
			params.put("id2", 8);
			n_cp=43;
			res=daoutil.queryForMapList(params);
			logger.info(res.toString());
			n_cp=45;
			MDC.put("action", "RequestCloser");
			n_cp=49;
			Map<String, Object> m_str;
			String t_req_id="";
			Object elem;
			String s_url=conf.getHost() + "/citizenrequest.html#citReqSearch";
			String s_email;
			String stat_text;
			for(int i= 0 ; i < res.size(); i++) {
				logger.info("=====" + i + "=====");
				n_cp=53;
				m_str = res.get(i);
				n_cp=55;
				d_stat=(Date) m_str.get("date_status");
				logger.info("date_status="+m_str.toString());
				t_req_id=m_str.get("id").toString();

				if (d_stat.getTime()<d_oper.getTime()){
					logger.info("true");
					//elem=m_str.get("id");
					//logger.info(String.valueOf(elem.getClass()));
					params.clear();
					params.put("sqlpath", "update_req_close");
					n_cp++;
					params.put("t_request_id", Double.valueOf(t_req_id));
					n_cp++;
					params.put("req_status", 5.0);
					n_cp++;
					params.put("rate_val", 0.0);
					n_cp++;
					params.put("rate_text", "");
					n_cp++;
					s_id=reqservice.req_hist(params, user);
					logger.info("s_id="+s_id);
					logger.info(params.toString());
					logger.info("------------------------");

					n_cp=121;
					Object[] values = new Object[]{5, 1};
					n_cp++;
					stat_text=daoutil.textByID("sprav/req_status_by_id",values);
					n_cp++;
					logger.info("t_user_id="+m_str.get("t_user_id").toString());
					s_email=daoutil.textByID("sprav/user_email",Double.valueOf(m_str.get("t_user_id").toString()));
					n_cp++;
					Map<String, String> par_send = new HashMap<>();
					par_send.put("MSG_CODE","REQ_STATUS");
					par_send.put("MSG_LANG","RU");
					par_send.put("EMAIL",s_email);
					par_send.put("REQ_ID",t_req_id);
					par_send.put("CUR_STAT",stat_text);
					par_send.put("INSTRUCTIONS_URL",s_url);
					n_cp++;
					userService.sendNotif(par_send);
				}

			}
			logger.trace("end unlock");
		}
		catch (Exception e) {
			MDC.put("action", "RequestCloser");
			logger.error("n_cp = "+ n_cp + ", exception = " + e.getMessage(), e);
		}
        MDC.clear();
	}

	@Scheduled(fixedDelay = 300000) // run every 5 minute
	public void runServletFBStatus() throws ServletException, IOException {
		try {
			List<Map<String, Object>> s_res = daoutil.queryForMapList("sprav/get_unpaid_payment");
			for (int i = 0; i < s_res.size(); i++) {
				Object[] values = null;
				URL url = new URL("http://epay.fortebank.com:8443/Exec");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty("Host", "epay.fortebank.com");
				conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
				conn.setRequestMethod("POST");
				OutputStream outputStream = conn.getOutputStream();
				String xmlOut = "<?xml version='1.0' encoding='UTF-8'?>\n" +
						"<TKKPG>\n" +
						"<Request>\n" +
						"<Operation>GetOrderStatus</Operation>\n" +
						"<Language>RU</Language>\n" +
						"<Order>\n" +
						"<Merchant>TOPKSK02002129</Merchant>\n" +
						"<OrderID>" + s_res.get(i).get("order_id") + "</OrderID>\n" +
						"</Order>\n" +
						"<SessionID>" + s_res.get(i).get("session_id") + "</SessionID>\n" +
						"</Request>\n" +
						"</TKKPG>";

				String compId = s_res.get(i).get("t_company_id").toString();
				StringReader xmlRead = new StringReader(xmlOut);
				IOUtils.copy(xmlRead, outputStream);
				outputStream.flush();
				String response1 = IOUtils.toString(conn.getInputStream(), "UTF-8");
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(response1)));
				NodeList nodesMain = doc.getElementsByTagName("Order");
				Element orderMain = (Element) nodesMain.item(0);
				String status = orderMain.getElementsByTagName("OrderStatus").item(0).getTextContent();
				if (!status.toUpperCase().equals("CREATED")&&!status.toUpperCase().equals("ON-PAYMENT")) {
					values = new Object[]{
							compId,
							s_res.get(i).get("order_id"),
							s_res.get(i).get("amount"),
							status.toUpperCase(),
							s_res.get(i).get("session_id")
					};
					String sqlCode = "requests/insert_t_pay";
					supplierService.getFBOrderInfo(sqlCode, values);
				}
			}
		}
		catch (Exception e) {
			logger.info(e.getMessage());
			logger.info("runServletFBStatus = "+e);
		}

	}

}
