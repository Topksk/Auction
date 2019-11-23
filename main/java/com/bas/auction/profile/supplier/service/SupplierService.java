package com.bas.auction.profile.supplier.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.supplier.dto.Supplier;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

public interface SupplierService {
    void register(UserCertInfo certInfo, Employee emp, Supplier supp) throws IOException;

    Supplier sendForApproval(User user, Long supplierId);

    Supplier confirmRegistration(User user, Long supplierId);

    Supplier approve(User user, Long supplierId);

    Supplier reject(User user, Long supplierId);

    List<Long> findEmptyNotificationSetting();

    void getFBOrderInfo(String sqlCode, Object[] values) throws ServletException, IOException;


}