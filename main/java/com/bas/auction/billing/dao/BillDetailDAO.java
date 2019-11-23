package com.bas.auction.billing.dao;

import com.bas.auction.billing.dto.BillDetail;

import java.util.List;

public interface BillDetailDAO {
    List<BillDetail> findBillDetails(Long billId);
}
