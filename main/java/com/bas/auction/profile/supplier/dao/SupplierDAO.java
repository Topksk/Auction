package com.bas.auction.profile.supplier.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.supplier.dto.Supplier;

import java.util.List;
import java.util.Map;

public interface SupplierDAO {
	String findBin(Long supplierId);

	String findName(Long supplierId);

	Supplier findById(User user, Long supplierId);

	String findRegStatus(Long supplierId);

	Supplier findBidSupplierById(Long supplierId);

	Supplier findUserOrg(User user);

	Supplier findUserOrgByid(Long supplierId);

	Long findIdByBin(String bin);

	boolean exists(String bin);

	boolean findIsNonresident(Long supplierId);

	boolean findIsIndividual(Long supplierId);

	String findSupplierCountry(Long supplierId);

	Supplier insert(User user, Supplier data);

	Supplier update(User user, Supplier data);

	Supplier updateRegStatus(User user, long id, String value);

	List<Map<String, Object>> findForIntegraNotSent();

	void setSent(Long supplierId);

	List<Long> findEmptyNotificationSetting();

	void insertFBStatus(String sqlCode, Object[] values);
}
