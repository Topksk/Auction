package com.bas.auction.core.servlets;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.DaoException;
import com.bas.auction.core.json.JsonUtils;
import com.bas.auction.core.spring.SpringInitServlet;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.plans.PlanTemplateGenerator;
import com.bas.auction.plans.dao.PlanFileDAO;
import com.bas.auction.plans.dto.PlanFile;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

@WebServlet(value = "/planim", name = "PlanImportService")
@MultipartConfig
public class PlanImportServlet extends SpringInitServlet {
	static {
		System.setProperty("javax.xml.parsers.SAXParserFactory",
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}

	private final static Logger logger = LoggerFactory.getLogger(PlanImportServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Autowired
	private transient JsonUtils json;
	@Autowired
	private transient PlanFileDAO planFileDAO;
	@Autowired
	private transient CustomerSettingDAO customerSettingsDAO;
	@Autowired
	private transient PlanTemplateGenerator planTemplateGenerator;
	@Autowired
	private transient Utils utils;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		logger.info("action: {}", action);
		MDC.put("action", action);
		PrintWriter w = null;
		try {
			User user = getCurrentUser();
			if ("gen_plan_template".equals(action)) {
				genTemplate(req, resp, user);
			} else if ("get_file_list".equals(action)) {
				resp.setContentType("application/json;charset=UTF-8");
				List<PlanFile> list = planFileDAO.findCustomerList(user.getCustomerId());
				w = resp.getWriter();
				utils.writeObject(w, list);
			}
		} catch (ApplException e) {
			logger.error("Exception in plan import get: action={}", action, e);
			utils.writeException(resp, e);
		} catch (DaoException e) {
			logger.error("Exception in plan import get: action={}", action, e);
			utils.writeException(resp, e);
		} catch (Exception e) {
			logger.error("Exception in plan import get: action={}", action, e);
		} finally {
			if (w != null) {
				w.flush();
				w.close();
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json;charset=UTF-8");
		PrintWriter w = resp.getWriter();
		try {
			User user = getCurrentUser();
			if (req.getContentType() != null && req.getContentType().contains("multipart/form-data")) {
				logger.info("action: upload plan");
				MDC.put("action", "upload plan");
				Part filePart = req.getPart("planfile");
				planFileDAO.create(user, filePart);
			}
			List<PlanFile> list = planFileDAO.findCustomerList(user.getCustomerId());
			utils.writeObject(w, list);
		} catch (ApplException e) {
			logger.error("Exception in plan import post", e);
			utils.writeException(resp, e);
		} catch (DaoException e) {
			logger.error("Exception in plan import post", e);
			utils.writeException(resp, e);
		} catch (Exception e) {
			logger.error("Exception in plan import post", e);
		} finally {
			w.flush();
			w.close();
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json;charset=UTF-8");
		PrintWriter w = resp.getWriter();
		String action = null;
		try (JsonReader jr = new JsonReader(req.getReader())) {
			Gson gson = utils.getGsonForClient();
			action = json.nextValue(gson, jr, String.class, "action");
			logger.info("action: {}", action);
			MDC.put("action", action);
			User user = getCurrentUser();
			if ("remove plan file".equals(action)) {
				long recid = json.nextValue(gson, jr, Long.class, "recid");
				planFileDAO.delete(user, recid);
			}
			List<PlanFile> list = planFileDAO.findCustomerList(user.getCustomerId());
			utils.writeObject(w, list);
		} catch (ApplException e) {
			logger.error("Exception in plan import delete: action={}", action, e);
			utils.writeException(resp, e);
		} catch (DaoException e) {
			logger.error("Exception in plan import delete: action={}", action, e);
			utils.writeException(resp, e);
		} catch (Exception e) {
			logger.error("Exception in plan import delete: action={}", action, e);
		} finally {
			w.flush();
			w.close();
		}
	}

	private void genTemplate(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
		resp.setContentType(PlanFileDAO.XlsxMimeType);
		long id = customerSettingsDAO.findMainId(user.getCustomerId());
		String contentDisposition = URLEncoder.encode("plan_template.xlsx", "UTF-8");
		String uagent = req.getHeader("User-Agent");
		if (uagent.contains("MSIE"))
			contentDisposition = "Attachment; Filename=" + contentDisposition;
		else
			contentDisposition = "Attachment; Filename*=UTF-8''" + contentDisposition;
		resp.setHeader("Content-disposition", contentDisposition);
		try (OutputStream os = new BufferedOutputStream(resp.getOutputStream())) {
			planTemplateGenerator.generateTemplate(id, os);
		}
	}
}
