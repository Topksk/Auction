package com.bas.auction.bid.replace.service;


import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dto.Bid;

import java.io.IOException;

public interface BidReplaceService {
    Bid replace(User user, Long bidId) throws IOException;
}
