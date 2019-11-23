package com.bas.auction.bid.withdraw.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;

public interface BidWithdrawService {
    Bid withdraw(User user, Long bidId);
}
