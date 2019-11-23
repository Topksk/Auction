package com.bas.auction.core.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.DaoException;
import com.bas.auction.core.json.ApplExceptionGsonAdapter;
import com.bas.auction.core.json.ClientExclusionStrategy;
import com.bas.auction.core.json.DaoExceptionGsonAdapter;
import com.bas.auction.core.json.SearchIndexExclusionStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class Utils {
	public static final String iso8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private final ApplExceptionGsonAdapter applExceptionGsonAdapter;
	private final DaoExceptionGsonAdapter daoExceptionGsonAdapter;

	@Autowired
	public Utils(ApplExceptionGsonAdapter applExceptionGsonAdapter, DaoExceptionGsonAdapter daoExceptionGsonAdapter) {
		this.applExceptionGsonAdapter = applExceptionGsonAdapter;
		this.daoExceptionGsonAdapter = daoExceptionGsonAdapter;
	}

	public static String getClientIpAddr(HttpServletRequest req) {
		String ip = req.getHeader("X-Forwarded-For");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = req.getRemoteAddr();
		}
		return ip;
	}

	public String camelCase(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		final char[] buffer = str.toLowerCase().toCharArray();
		boolean capitalizeNext = false;
		for (int i = 0; i < buffer.length; i++) {
			final char ch = buffer[i];
			if (ch == '_') {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer[i] = Character.toTitleCase(ch);
				capitalizeNext = false;
			}
		}
		str = new String(buffer);
		return str.replace("_", "");
	}

	public String camelCaseToUnderscore(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (!Character.isUpperCase(ch)) {
				sb.append(ch);
				continue;
			}
			ch = Character.toLowerCase(ch);
			sb.append('_').append(ch);
		}
		return sb.toString();
	}

	public void writeException(HttpServletResponse resp, ApplException e) throws IOException {
		if (e.getStatusCode() > 0)
			resp.setStatus(e.getStatusCode());
		else
			resp.setStatus(HttpServletResponse.SC_CONFLICT);
		resp.setContentType("application/json;charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		Gson gson = getGsonForClient();
		writer.write(gson.toJson(e));
	}

	public void writeException(HttpServletResponse resp, DaoException e) throws IOException {
		resp.setStatus(HttpServletResponse.SC_CONFLICT);
		resp.setContentType("application/json;charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		Gson gson = getGsonForClient();
		writer.write(gson.toJson(e));
	}

	public void writeObject(PrintWriter w, Object o) {
		Gson gson = getGsonForClient();
		w.write(gson.toJson(o));
	}

	public void writeObject(PrintWriter w, String tag, Object o) {
		Gson gson = getGsonForClient();
		w.write("{\"");
		w.write(tag);
		w.write("\":");
		w.write(gson.toJson(o));
		w.write("}");
	}

	public void writeRecord(PrintWriter w, Object o) {
		writeObject(w, "record", o);
	}

	public void writeRecords(PrintWriter w, Collection<?> o) {
		writeObject(w, "records", o);
	}

	private static GsonBuilder getCommonGson() {
		return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.setDateFormat(iso8601);
	}

	public Gson getGsonForClient() {
		return getCommonGson().registerTypeHierarchyAdapter(ApplException.class, applExceptionGsonAdapter)
				.registerTypeAdapter(DaoException.class, daoExceptionGsonAdapter)
				.setExclusionStrategies(new ClientExclusionStrategy()).create();
	}

	public static Gson getGsonForSearchIndex() {
		return getCommonGson().setExclusionStrategies(new SearchIndexExclusionStrategy()).create();
	}

}
