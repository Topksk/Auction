package com.bas.auction.profile.supplier.service;


public interface SupplierNotificationService {
    void sendRegStatusMail(long supplierId, boolean approved, String rejectReason);
}
