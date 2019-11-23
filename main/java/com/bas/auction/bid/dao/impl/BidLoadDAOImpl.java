package com.bas.auction.bid.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.dao.BidLoadDAO;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.google.gson.Gson;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BidLoadDAOImpl implements BidLoadDAO, GenericDAO<Bid> {
	private final Logger logger = LoggerFactory.getLogger(BidLoadDAOImpl.class);
	private final DaoJdbcUtil daoutil;
	private final DocFileDAO docFileDAO;
	private final ExchangeRateService exchangeRateService;
	private final BidLineDAO bidLineDAO;
	private final Gson gson;
	private Client client;
	private BulkRequestBuilder bulkRequest;
	private final Map<String, BigDecimal> rates = new HashMap<>();

	@Autowired
	public BidLoadDAOImpl(DaoJdbcUtil daoutil, ExchangeRateService exchangeRateService, BidLineDAO bidLineDAO,
			DocFileDAO docFileDAO) {
		this.daoutil = daoutil;
		this.exchangeRateService = exchangeRateService;
		this.bidLineDAO = bidLineDAO;
		this.docFileDAO = docFileDAO;
		this.gson = Utils.getGsonForSearchIndex();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	private void setDraftBidExchangeRate(Bid bid) {
		if ("DRAFT".equals(bid.getBidStatus())) {
			String ccode = bid.getCurrencyCode();
			BigDecimal rate = rates.get(ccode);
			if (rate == null) {
				// while bid is draft always return actual exchange rate
				rate = exchangeRateService.findCurrentExchangeRate(ccode);
				rates.put(ccode, rate);
				if (rate == null) {
					logger.error("no exchange rate: bidId = {}, currency = {}", bid.getBidId(), bid.getCurrencyCode());
					throw new ApplException("NO_EXCHANGE_RATE");
				}
			}
			bid.setSentExchangeRate(rate);
			bid.setUnlockExchangeRate(rate);
		}
	}

	private void setBidLines(Bid bid) {
		Long bidId = bid.getBidId();
		List<BidLine> bidLines = bidLineDAO.findBidLines(bidId);
		bid.setBidLines(bidLines);
	}

	private void setBidFiles(User user, Bid bid) {
		Long bidId = bid.getBidId();
		List<DocFile> bidFiles = docFileDAO.findByAttr(user, "bid_id", bidId);
		bid.setBidFiles(bidFiles);
	}

	@Override
	public Class<Bid> getEntityType() {
		return Bid.class;
	}

	private List<Bid> findAllBids() {
		User user = new User(1L);
		user.setSysadmin(true);
		List<Bid> bids = daoutil.query(this, "list_all_bids");
		bids.forEach(bid -> {
			setDraftBidExchangeRate(bid);
			setBidLines(bid);
			setBidFiles(user, bid);
		});
		return bids;
	}

	@Override
	public void indexAllBids(Client client) {
		logger.info("start indexing all bids");
		this.client = client;
		long start = new Date().getTime();
		List<Bid> allbids = findAllBids();
		int i = 0;
		bulkRequest = client.prepareBulk();
		for (Bid bid : allbids) {
			i++;
			addToBulk(bid);
			if (i == 500) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		bulkRequest.execute().actionGet();
		long end = new Date().getTime();
		logger.info("finished indexing bids. {} seconds", (end - start) / 1000);
	}

	private void addToBulk(Bid bid) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.AUCTION_INDEX, "bids",
				String.valueOf(bid.getBidId()));
		req.setSource(gson.toJson(bid));
		bulkRequest.add(req);
	}
}
