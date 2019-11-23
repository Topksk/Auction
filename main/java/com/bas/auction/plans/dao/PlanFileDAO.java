package com.bas.auction.plans.dao;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.Part;

import com.bas.auction.auth.dto.User;
import com.bas.auction.plans.dto.PlanFile;

public interface PlanFileDAO {
	final String XlsMimeType = "application/vnd.ms-excel";
	final String XlsxMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	PlanFile findById(Long id);

	List<PlanFile> findCustomerList(Long customerId);

	void create(User user, Part filePart) throws IOException;

	void delete(User user, Long id) throws IOException;
}
