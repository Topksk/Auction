package com.bas.auction.neg.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.search.SearchService;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegTeamDAO;
import com.bas.auction.neg.dao.NegotiationLoadDAO;
import com.bas.auction.neg.dto.NegLine;
import com.bas.auction.neg.dto.NegTeam;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.service.NegFileService;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NegotiationLoadDAOImpl implements NegotiationLoadDAO, GenericDAO<Negotiation> {
	private final Logger logger = LoggerFactory.getLogger(NegotiationLoadDAOImpl.class);
	private final DaoJdbcUtil daoutil;
	private final Gson gson;
	private final BidDAO biddao;
	private final NegLineDAO negLineDAO;
	private final NegTeamDAO negTeamDAO;
	private final NegFileService negFileService;
	private Client client;
	private BulkRequestBuilder bulkRequest;

	@Autowired
	public NegotiationLoadDAOImpl(DaoJdbcUtil daoutil, BidDAO biddao, NegLineDAO negLineDAO, NegTeamDAO negTeamDAO,
			NegFileService negFileService) {
		this.daoutil = daoutil;
		this.biddao = biddao;
		this.negLineDAO = negLineDAO;
		this.negFileService = negFileService;
		this.negTeamDAO = negTeamDAO;
		this.gson = Utils.getGsonForSearchIndex();
	}

	@Override
	public String getSqlPath() {
		return "load";
	}

	@Override
	public Class<Negotiation> getEntityType() {
		return Negotiation.class;
	}

	private void setBidIds(Negotiation neg) {
		List<Map<String, Long>> bidIds = biddao.findNegBidIds(neg.getNegId());
		neg.setBidIds(bidIds);
	}

	private void setNegLines(Negotiation neg) {
		Long negId = neg.getNegId();
		List<NegLine> negLines = negLineDAO.findNegLines(negId);
		neg.setNegLines(negLines);
	}

	private void setNegTeam(Negotiation neg) {
		Long negId = neg.getNegId();
		List<NegTeam> negTeam = negTeamDAO.findNegTeam(negId);
		neg.setNegTeam(negTeam);
	}

	private void setNegFiles(User user, Negotiation neg) {
		Long negId = neg.getNegId();
		List<DocFile> negFiles = negFileService.findNegFiles(user, negId);
		neg.setNegFiles(negFiles);
	}

	private List<Negotiation> findAllNegs() {
		User user = new User(1L);
		user.setSysadmin(true);
		List<Negotiation> negs = daoutil.query(this, "list_all_negs");
		negs.forEach(neg -> {
			setBidIds(neg);
			setNegLines(neg);
			setNegTeam(neg);
			setNegFiles(user, neg);
		});
		return negs;
	}

	@Override
	public void indexAllNegs(Client client) {
		logger.info("start indexing all negs");
		this.client = client;
		long start = new Date().getTime();
		List<Negotiation> allnegs = findAllNegs();
		int i = 0;
		bulkRequest = client.prepareBulk();
		for (Negotiation neg : allnegs) {
			i++;
			addToBulk(neg);
			if (i == 500) {
				bulkRequest.execute().actionGet();
				bulkRequest = client.prepareBulk();
				i = 0;
			}
		}
		bulkRequest.execute().actionGet();
		long end = new Date().getTime();
		logger.info("finished indexing negs. {} seconds", (end - start) / 1000);
	}

	private void addToBulk(Negotiation neg) {
		IndexRequestBuilder req = client.prepareIndex(SearchService.AUCTION_INDEX, "negs",
				String.valueOf(neg.getNegId()));
		req.setSource(gson.toJson(neg));
		bulkRequest.add(req);
	}
}
