package com.bas.auction.core.servlets;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.crypto.CertValidationError;
import com.bas.auction.core.dao.DaoException;
import com.bas.auction.core.spring.SpringInitServlet;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.core.utils.ZipUtils;
import com.bas.auction.digsign.DigitalSignatureArchiveProcessorService;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dto.DocFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@WebServlet(value = "/fileservice", name = "FileService")
@MultipartConfig
public class FileServlet extends SpringInitServlet {
	private final static Logger logger = LoggerFactory.getLogger(FileServlet.class);
	private static final long serialVersionUID = 6306566319619390707L;
	private static final int BUFFER_SIZE = 8192;

	@Autowired
	private transient DocFileDAO docFileDAO;
    @Autowired
    private transient DigitalSignatureArchiveProcessorService digitalSignatureArchiveProcessorService;
    @Autowired
	private transient DocFileSignatureDAO docFileSignatureDAO;
	@Autowired
	private transient Conf conf;
	@Autowired
	private transient Utils utils;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if (action != null) {
			logger.info("action: {}", action);
			MDC.put("action", action);
		} else {
			logger.info("action: get_file");
			MDC.put("action", "get_file");
		}
		String fileIdParam = req.getParameter("fileId");
		PrintWriter w = null;
		try {

			User user = getCurrentUser();
			if ("get_files_by_attr".equals(action)) {
				resp.setContentType("application/json;charset=UTF-8");
				w = resp.getWriter();
				String attribute = req.getParameter("attr");
				String value = req.getParameter("value");
				logger.debug("{} = {}", attribute, value);
				writeFileList(w, attribute, value, user);
			} else if ("file_signatures".equals(action)) {
				resp.setContentType("application/json;charset=UTF-8");
				w = resp.getWriter();
				long fileId = Long.parseLong(fileIdParam);
				logger.debug("file id: {}", fileId);
				writeFileSignatureList(w, fileId);
			} else if ("get_certificate".equals(action)) {
				long certId = Long.parseLong(req.getParameter("certId"));
				logger.debug("cert id: {}", certId);
				writeCertContent(req, resp, certId);
			} else if ("get_signature".equals(action)) {
				long signId = Long.parseLong(req.getParameter("signId"));
				logger.debug("sign id: {}", signId);
				writeSignContent(req, resp, signId);
			} else if ("download_all".equals(action)) {
				String attribute = req.getParameter("attr");
				String value = req.getParameter("value");
				logger.debug("{} = {}", attribute, value);
				writeAllFileContents(resp, attribute, value, user);
			} else if (fileIdParam != null) {
				long fileId = Long.parseLong(fileIdParam);
				logger.debug("file id: {}", fileId);
				writeFileContent(req, resp, fileId, user);
			}

		} catch (ApplException e) {
			logger.error("Exception in file service get: action={}", action, e);
			utils.writeException(resp, e);
		} catch (DaoException e) {
			logger.error("Exception in file service get: action={}", action, e);
			utils.writeException(resp, e);
		} catch (Exception e) {
			logger.error("Exception in file service get: action={}", action, e);
			resp.getWriter().println("{}");
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
		String action = req.getParameter("action");
		if (action != null) {
			logger.info("action: {}", action);
			MDC.put("action", action);
		}
		PrintWriter w = resp.getWriter();
		try {

			User user = getCurrentUser();
			if (req.getContentType() != null && req.getContentType().contains("multipart/form-data")) {
				if (req.getParameter("NotSignedFile") != null) {
					logger.debug("action: upload not signed file");
					MDC.put("action", "upload not signed file");
                    digitalSignatureArchiveProcessorService.processArchiveWithUnsignedFile(req, user.getUserId());
                } else if (req.getParameter("SignatureOnly") != null) {
					logger.debug("action: upload signature only");
					MDC.put("action", "upload signature only");
                    digitalSignatureArchiveProcessorService.processArchiveWithSignature(req, user.getUserId());
                } else {
					logger.debug("action: upload signed file");
					MDC.put("action", "upload signed file");
                    digitalSignatureArchiveProcessorService.processArchiveWithSignedFile(req, user.getUserId());
                }

				w.println("{}");
			} else if ("remove".equals(action)) {
				String idparam = req.getParameter("fileIds");
				String[] ids = idparam.split(",");
				logger.debug("ids: ", idparam);
				String attribute = req.getParameter("attr");
				String value = req.getParameter("value");
				logger.debug("{} = {}", attribute, value);
				for (String id : ids) {
					long fileId = Long.parseLong(id);
					docFileDAO.delete(user, fileId);
				}
				writeFileList(w, attribute, value, user);
			}

		} catch (ApplException e) {
			logger.error("Exception in file service post: action={}", action, e);
			utils.writeException(resp, e);
		} catch (DaoException e) {
			logger.error("Exception in file service post: action={}", action, e);
			utils.writeException(resp, e);
		} catch (CertValidationError e) {
			resp.setContentType("text/plain; charset=UTF8");
			w.println("error: " + e.getType());
		} catch (Exception e) {
			logger.error("Exception in file service post: action={}", action, e);
			w.println("{}");
		} finally {
			w.flush();
			w.close();
		}
	}

	private void writeFileList(PrintWriter w, String attribute, String value, User user) {
		utils.writeObject(w, docFileDAO.findByAttr(user, attribute, value));
	}

	private void writeFileSignatureList(PrintWriter w, long fileId) {
		utils.writeObject(w, docFileSignatureDAO.findFileSignatures(fileId));
	}

	private void writeFileContent(HttpServletRequest req, HttpServletResponse resp, long fileId, User user)
			throws IOException {
		DocFile doc = docFileDAO.findById(user, fileId);
		if (doc == null)
			return;
		Path path = doc.getFile();
		resp.setContentType(doc.getFileType());
		try (BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream());
				BufferedInputStream is = new BufferedInputStream(Files.newInputStream(path))) {
			String contentDisposition = URLEncoder.encode(doc.getFileName(), "UTF-8");
			String uagent = req.getHeader("User-Agent");
			if (uagent.contains("MSIE"))
				contentDisposition = "Attachment; Filename=" + contentDisposition;
			else
				contentDisposition = "Attachment; Filename*=UTF-8''" + contentDisposition;
			resp.setHeader("Content-disposition", contentDisposition);
			resp.setHeader("Content-Length", String.valueOf(Files.size(path)));
			byte[] buff = new byte[BUFFER_SIZE];
			int count;
			while ((count = is.read(buff)) > 0) {
				out.write(buff, 0, count);
				out.flush();
			}
		}
	}

	private void writeCertContent(HttpServletRequest req, HttpServletResponse resp, long certId) throws IOException {
		resp.setContentType("application/octet-string");
		String contentDisposition = "usert_cert.cer";
		String uagent = req.getHeader("User-Agent");
		if (uagent.contains("MSIE"))
			contentDisposition = "Attachment; Filename=" + contentDisposition;
		else
			contentDisposition = "Attachment; Filename*=UTF-8''" + contentDisposition;
		resp.setHeader("Content-disposition", contentDisposition);

		Path path = Paths.get(conf.getUserCertsStorePath() + certId + ".cer");
		resp.setHeader("Content-Length", String.valueOf(Files.size(path)));
		byte[] buff = new byte[BUFFER_SIZE];
		int count;
		try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(path));
				BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream())) {
			while ((count = is.read(buff)) > 0) {
				out.write(buff, 0, count);
				out.flush();
			}
		}
	}

	private void writeSignContent(HttpServletRequest req, HttpServletResponse resp, long signId) throws IOException {
		resp.setContentType("application/octet-string");
		String contentDisposition = "sign.p7s";
		String uagent = req.getHeader("User-Agent");
		if (uagent.contains("MSIE"))
			contentDisposition = "Attachment; Filename=" + contentDisposition;
		else
			contentDisposition = "Attachment; Filename*=UTF-8''" + contentDisposition;
		resp.setHeader("Content-disposition", contentDisposition);

		byte[] signature = docFileSignatureDAO.findSignatureBody(signId);
		if (signature == null)
			return;
		resp.setHeader("Content-Length", String.valueOf(signature.length));
		try (BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream())) {
			out.write(signature);
			out.flush();
			out.close();
		}
	}

	private void writeAllFileContents(HttpServletResponse resp, String attribute, String value, User user)
			throws IOException, ZipException {
		List<DocFile> files = docFileDAO.findByAttr(user, attribute, value);
		File zip = ZipUtils.zipFiles(files);
		resp.setContentType("application/zip");
		resp.setHeader("Content-Disposition", "Attachment; Filename=files.zip");
		try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(zip));
				BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream())) {
			int count;
			byte[] buff = new byte[BUFFER_SIZE];
			while ((count = is.read(buff)) > 0) {
				out.write(buff, 0, count);
				out.flush();
			}
		} finally {
			Files.deleteIfExists(zip.toPath());
		}
	}
}
