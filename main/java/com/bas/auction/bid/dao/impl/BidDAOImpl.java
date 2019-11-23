package com.bas.auction.bid.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.dao.BidLineDAO;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.dto.BidLine;
import com.bas.auction.bid.service.BidFileService;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.currency.service.ExchangeRateService;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.profile.address.dto.Address;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.employee.dao.EmployeeDAO;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Repository
public class BidDAOImpl implements BidDAO, GenericDAO<Bid> {
    private final Logger logger = LoggerFactory.getLogger(BidDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final Conf conf;
    private final SearchService searchService;
    private final ExchangeRateService exchangeRateService;
    private final BidLineDAO bidLineDAO;
    private final SupplierDAO supplierDAO;
    private final EmployeeDAO employeeDAO;
    private final AddressService addressService;
    private NegotiationDAO negotiationDAO;
    private final BidFileService bidFileService;

    @Autowired
    public BidDAOImpl(DaoJdbcUtil daoutil, SearchService searchService, Conf conf,
                      ExchangeRateService exchangeRateService, BidLineDAO bidLineDAO, SupplierDAO supplierDAO,
                      EmployeeDAO employeeDAO, AddressService addressService, BidFileService bidFileService) {
        this.daoutil = daoutil;
        this.searchService = searchService;
        this.conf = conf;
        this.exchangeRateService = exchangeRateService;
        this.bidLineDAO = bidLineDAO;
        this.supplierDAO = supplierDAO;
        this.employeeDAO = employeeDAO;
        this.addressService = addressService;
        this.bidFileService = bidFileService;
    }

    @Autowired
    private void setNegotiationDAO(NegotiationDAO negotiationDAO) {
        this.negotiationDAO = negotiationDAO;
    }

    @Override
    public String getSqlPath() {
        return "bids";
    }

    @Override
    public Class<Bid> getEntityType() {
        return Bid.class;
    }

    @Override
    public Bid findAndIndexSync(User user, Long bidId) {
        Bid bid = findById(user, bidId);
        indexSync(bid);
        return bid;
    }

    @Override
    public void indexSync(Bid bid) {
        searchService.indexSync("bids", bid.getBidId(), bid);
    }

    private void updateAsync(Bid bid) {
        searchService.updateAsync("bids", bid.getBidId(), bid);
    }

    @Override
    public void reindexNegBidHeaders(Long negId) {
        daoutil.query(this, "get_neg_bids", negId).forEach(this::updateAsync);
    }

    @Override
    public void deleteFromSearchIndexSync(Long bidId) {
        searchService.deleteSync("bids", bidId);
    }

    @Override
    public boolean findIsDraft(Long bidId) {
        return daoutil.exists(this, "is_draft_bid", bidId);
    }

    @Override
    public String findBidCurrency(Long bidId) {
        return daoutil.queryScalar(this, "get_bid_currency_code", bidId);
    }

    @Override
    public Bid findById(User user, Long bidId) {
        logger.debug("get bid: bidId = {}", bidId);
        Bid bid = daoutil.queryForObject(this, "get", bidId, user.getSupplierId());
        if (bid == null) {
            logger.warn("bid not found: bidId = {}", bidId);
            return null;
        }
        setDraftBidExchangeRate(bid);
        setBidLines(bid);
        setBidFiles(user, bid);
        Negotiation neg = negotiationDAO.findBidNeg(user, bid);
        bid.setNeg(neg);
        return bid;
    }

    private void setBidLines(Bid bid) {
        Long bidId = bid.getBidId();
        List<BidLine> bidLines = bidLineDAO.findBidLines(bidId);
        bid.setBidLines(bidLines);
    }

    private void setBidFiles(User user, Bid bid) {
        Long bidId = bid.getBidId();
        List<DocFile> bidFiles = bidFileService.findBidFiles(user, bidId);
        bid.setBidFiles(bidFiles);
    }

    private void setDraftBidExchangeRate(Bid bid) {
        if (bid.isDraft()) {
            // while bid is draft always set actual exchange rate
            BigDecimal rate = exchangeRateService.findCurrentExchangeRate(bid.getCurrencyCode());
            if (rate == null) {
                logger.error("no exchange rate: bidId = {}, currency = {}", bid.getBidId(), bid.getCurrencyCode());
                throw new ApplException("NO_EXCHANGE_RATE");
            }
            bid.setSentExchangeRate(rate);
            bid.setUnlockExchangeRate(rate);
        }
    }

    @Override
    public Long findBidNegId(Long bidId) {
        logger.debug("get bid negId: {}", bidId);
        Long id = daoutil.queryScalar(this, "get_bid_neg_id", bidId);
        if (id == null || id < 1)
            logger.warn("bid negId not found: bidId = {}", bidId);
        return id;
    }

    @Override
    public List<Map<String, Long>> findNegBidIds(Long negId) {
        logger.debug("get neg bidIds: {}", negId);
        return daoutil.queryForTypedMapList(this, "get_neg_bid_ids", negId);
    }

    private BigDecimal convertBidLinePrice(BigDecimal price, BigDecimal exchangeRate) {
        if (price == null) {
            return null;
        }
        return price.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void setNegBidLinesWithConvertedPrices(Bid bid) {
        setBidLines(bid);
        bid.getBidLines().forEach(line -> {
            BigDecimal convertedPrice = convertBidLinePrice(line.getBidPrice(), bid.getUnlockExchangeRate());
            line.setBidPrice(convertedPrice);
            convertedPrice = convertBidLinePrice(line.getTotalPrice(), bid.getUnlockExchangeRate());
            line.setTotalPrice(convertedPrice);
            convertedPrice = convertBidLinePrice(line.getTotalDiscountPrice(), bid.getUnlockExchangeRate());
            line.setTotalDiscountPrice(convertedPrice);
        });
    }

    private void setNegBidSupplierInfo(Bid bid) {
        Supplier supplier = supplierDAO.findBidSupplierById(bid.getSupplierId());
        bid.setSupplier(supplier);
        List<Address> supplierAddrs = addressService.findSupplierAddresses(bid.getSupplierId());
        Address legalAddr = supplierAddrs.stream().filter(Address::isLegalAddress).findFirst().orElse(null);
        Address physicalAddr = supplierAddrs.stream().filter(Address::isPhysicalAddress).findFirst().orElse(null);
        bid.setSupplierLegalAddress(legalAddr);
        bid.setSupplierPhysicalAddress(physicalAddr);
    }

    private void setBidAuthorEmployee(Bid bid) {
        bid.setAuthorEmployee(employeeDAO.findBidAuthorEmployee(bid.getCreatedBy()));
    }

    @Override
    public List<Bid> findNegBids(User user, Long negId) {
        logger.debug("get neg bids: {}", negId);
        List<Bid> bids = daoutil.query(this, "get_neg_bids", negId);
        if (bids != null) {
            bids.forEach(bid -> {
                setNegBidLinesWithConvertedPrices(bid);
                setNegBidSupplierInfo(bid);
                setBidFiles(user, bid);
                setBidAuthorEmployee(bid);
            });
        } else
            logger.info("neg bids is empty: {}", negId);
        return bids;
    }

    @Override
    public Map<Long, Date> findPublishDates(List<Long> bidIds) {
        return bidIds.stream().map(this::mapToPublishDateEntry).collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Long, Date> mapToPublishDateEntry(Long bidId) {
        Date publishDate = findPublishDate(bidId);
        return new SimpleEntry<>(bidId, publishDate);
    }

    private Date findPublishDate(Long bidId) {
        return daoutil.queryScalar(this, "get_bid_publish_date", bidId);
    }

    @Override
    public String findBidStatus(Long bidId) {
        return daoutil.queryScalar(this, "get_bid_status", bidId);
    }

    @Override
    @SpringTransactional
    public void updateNegActiveBidsAwardStatuses(Long userId, Long negId) {
        Stream<Entry<Long, String>> awardStatuses = findNegActiveBidsAwardStatuses(negId).entrySet().stream();
        List<Object[]> params = awardStatuses
                .map(entry -> mapToActiveBidStatusUpdateParams(userId, entry))
                .collect(toList());
        daoutil.batchDML(this, "update_bid_status", params);
    }

    private Map<Long, String> findNegActiveBidsAwardStatuses(Long negId) {
        List<Map<String, Object>> statuses = daoutil.queryForMapList(this, "get_neg_active_bids_award_statuses", negId);
        return statuses.stream()
                .map(this::mapToBidAwardStatus)
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private Entry<Long, String> mapToBidAwardStatus(Map<String, Object> map) {
        Long bidId = (Long) map.get("bid_id");
        Boolean wonSomeLines = (Boolean) map.get("won_some_lines");
        Boolean wonAllLines = (Boolean) map.get("won_all_lines");
        String status;
        if (wonAllLines)
            status = "AWARDED";
        else if (wonSomeLines)
            status = "PARTIAL";
        else
            status = "FAILED";
        return new SimpleEntry<>(bidId, status);
    }

    private Object[] mapToActiveBidStatusUpdateParams(Long userId, Entry<Long, String> entry) {
        return new Object[]{entry.getValue(), userId, entry.getKey()};
    }

    private List<Map<String, Object>> findLastBidsWithStatuses(Long supplierId, String... statuses) {
        logger.debug("get last bids: supplierId = {}, statuses = {}", supplierId, statuses);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String status : statuses) {
            result.addAll(daoutil.queryForMapList(this, "get_last_with_status", supplierId, status));
        }
        result.sort((x, y) -> {
            Long bidId1 = (Long) x.get("recid");
            Long bidId2 = (Long) y.get("recid");
            return bidId2.compareTo(bidId1);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> findLastDraftBids(Long supplierId) {
        return findLastBidsWithStatuses(supplierId, "DRAFT");
    }

    @Override
    public List<Map<String, Object>> findLastActiveBids(Long supplierId) {
        return findLastBidsWithStatuses(supplierId, "ACTIVE");
    }

    @Override
    public List<Map<String, Object>> findLastAwardedBids(Long supplierId) {
        return findLastBidsWithStatuses(supplierId, "AWARDED", "PARTIAL");
    }

    @Override
    public List<Map<String, Object>> findLastRejectedBids(Long supplierId) {
        return findLastBidsWithStatuses(supplierId, "REJECTED", "FAILED");
    }

    @Override
    public List<Map<String, Object>> findPublishedTender2Stage2Negs(Long supplierId) {
        return daoutil.queryForMapList(this, "get_published_tender2_stage2_negs", supplierId);
    }

    @Override
    public List<Entry<Long, String>> findNegBidsCurrencyCodes(Long negId) {
        List<Map<String, Object>> bidCurrencies = daoutil.queryForMapList(this, "get_active_foreign_currency_bids",
                negId, conf.getFunctionalCurrency());
        return bidCurrencies.stream().map(this::makeBidEntry).collect(Collectors.toList());
    }

    private Entry<Long, String> makeBidEntry(Map<String, Object> entry) {
        return new SimpleEntry<>((Long) entry.get("bid_id"), (String) entry.get("currency_code"));
    }

    @Override
    public List<Map<String, Object>> findNegAndBidLinesPrices(Long bidId) {
        return daoutil.queryForMapList(this, "get_neg_and_bid_line_prices", bidId);
    }

    @Override
    public List<Map<String, Object>> findNegLinesUnitAndBidLinesMeanPrices(Long negId) {
        return daoutil.queryForMapList(this, "get_neg_lines_unit_and_bid_lines_mean_prices", negId);
    }

    @Override
    public Long findSupplierTender2Stage1PermittedBidId(Long supplierId, Long negId) {
        return daoutil.queryScalar(this, "get_tender2_stage1_permitted_bid_id", supplierId, negId);
    }

    @Override
    public Long findSupplierActiveBidId(Long supplierId, Long negId) {
        return daoutil.queryScalar(this, "get_supplier_active_bid_id", negId, supplierId);
    }

    @Override
    public boolean activeBidExists(Long supplierId, Long negId) {
        return daoutil.exists(this, "active_supplier_bid_exists", negId, supplierId);
    }

    @Override
    public boolean findIsBidReplaced(Long bidId) {
        return daoutil.exists(this, "is_bid_replaced", bidId);
    }

    @Override
    public Long findReplacedBidId(Long bidId) {
        return daoutil.queryScalar(this, "get_replaced_bid_id", bidId);
    }

    @Override
    public Long findReplacingBidId(Long bidId) {
        return daoutil.queryScalar(this, "get_replacing_bid_id", bidId);
    }

    @Override
    public Map<Integer, BigDecimal> findAuctionBidCurrentBestPrices(Long negId) {
        String funcCurrency = conf.getFunctionalCurrency();
        List<Map<String, Object>> auctionCurrentBestPrices = daoutil.queryForMapList(this, "auction_current_best_prices", funcCurrency, negId);
        return auctionCurrentBestPrices.stream()
                .collect(toMap(e -> (Integer) e.get("line_num"), e -> (BigDecimal) e.get("best_price")));
    }

    @Override
    public BigDecimal findAuctionBidLineCurrentBestPrice(Long negId, Integer lineNum) {
        String funcCurrency = conf.getFunctionalCurrency();
        return daoutil.queryScalar(this, "auction_line_current_best_price", funcCurrency, negId, lineNum);
    }

    @Override
    @SpringTransactional
    public Bid insert(User user, Bid bid) {
        Object[] values = {bid.getNegId(), bid.getSupplierId(), bid.getBidStatus(), bid.getCurrencyCode(),
                user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        bid.setBidId((Long) kh.getKeys().get("bid_id"));
        return bid;
    }

    @Override
    @SpringTransactional
    public Long copyBidForReplace(User user, Long sourceBidId) {
        Object[] values = {user.getUserId(), user.getUserId(), sourceBidId};
        KeyHolder keyHolder = daoutil.dml(this, "copy_bid_for_replace", values);
        return (Long) keyHolder.getKeys().get("bid_id");
    }

    @Override
    @SpringTransactional
    public void update(User user, Bid bid) {
        Object[] values = {bid.getBidLimitDays(), bid.getBidComments(), user.getUserId(), bid.getBidId(),
                user.getSupplierId()};
        daoutil.update(this, values);
    }

    @Override
    @SpringTransactional
    public void updateBidUnlockExchangeRate(Long bidId, BigDecimal exchangeRate) {
        Object[] values = {exchangeRate, bidId};
        daoutil.dml(this, "update_bid_unlock_exchange_rate", values);
    }

    @Override
    @SpringTransactional
    public void updateCurrency(User user, Long bidId, String currency) {
        logger.debug("update bid currency: bidId = {}, currency = {}", bidId, currency);
        Object[] values = {currency, user.getUserId(), bidId};
        daoutil.dml(this, "update_currency", values);
    }

    @Override
    @SpringTransactional
    public void updateStatus(Long userId, Long bidId, String status) {
        Object[] values = {status, userId, bidId};
        daoutil.dml(this, "update_bid_status", values);
    }

    @Override
    public void updateAllBidsUnlockExchangeRate(Long negId) {
        logger.debug("update all bids exchange rates: negId = {}", negId);
        Object[] values = {conf.getFunctionalCurrency(), negId};
        daoutil.dml(this, "update_all_bids_unlock_exchange_rates", values);
    }

    @Override
    @SpringTransactional
    public void updateBidExchangeRate(User user, Long bidId, BigDecimal exchangeRate) {
        logger.debug("update bid exchange rate: bidId = {}", bidId);
        Object[] values = {exchangeRate, exchangeRate, user.getUserId(), bidId};
        daoutil.dml(this, "update_bid_exchange_rate", values);
    }

    @Override
    @SpringTransactional
    public void updateBidForPublish(Long userId, Long bidId, BigDecimal rate) {
        Object[] values = {"ACTIVE", rate, rate, userId, bidId};
        daoutil.dml(this, "update_bid_for_publish", values);
    }

    @Override
    @SpringTransactional
    public void delete(User user, Long bidId) throws IOException {
        Object[] values = {bidId};
        daoutil.delete(this, values);
    }

    @Override
    public Map<Integer, BigDecimal> findSalesroomBestPrices(Long negId) {
        String funcCur = conf.getFunctionalCurrency();
        List<Map<String, Object>> list = daoutil.queryForMapList(this, "get_salesroom_best_prices", funcCur, negId);
        return list.stream()
                .filter(p -> Objects.nonNull(p.get("best_price")))
                .collect(toMap(e -> (Integer) e.get("line_num"), this::mapToRoundedPrice));
    }

    private BigDecimal mapToRoundedPrice(Map<String, Object> bidLinePrice) {
        BigDecimal bestPrice = (BigDecimal) bidLinePrice.get("best_price");
        return bestPrice.setScale(2, RoundingMode.DOWN);
    }

    @Override
    public Map<Long, Map<Integer, Long>> findAllSuppliersRanks(Long negId) {
        List<Map<String, Object>> res = daoutil.queryForMapList(this, "get_all_suppliers_rank",
                conf.getFunctionalCurrency(), negId);
        return res.stream()
                .collect(groupingBy(e -> (Long) e.get("supplier_id"),
                        toMap(e -> (Integer) e.get("line_num"), e -> (Long) e.get("rank"))));
    }

    @Override
    public Map<Integer, Long> findSupplierRanks(Long negId, Long supplierId) {
        String funcCurr = conf.getFunctionalCurrency();
        List<Map<String, Object>> res = daoutil.queryForMapList(this, "get_supplier_ranks", funcCurr, negId, supplierId);
        return res.stream()
                .collect(toMap(e -> (Integer) e.get("line_num"), e -> (Long) e.get("rank")));
    }

    @Override
    public Map<Integer, Map<String, BigDecimal>> findSupplierLastSaleroomBids(Long negId, Long supplierId, String currencyCode) {
        BigDecimal currencyExchangeRate = exchangeRateService.findCurrentExchangeRate(currencyCode);
        String funcCurr = conf.getFunctionalCurrency();
        List<Map<String, Object>> bidLines = daoutil.queryForMapList(this, "get_supp_last_salesroom_bids", funcCurr, negId,
                supplierId);
        return bidLines.stream()
                .collect(toMap(e -> (Integer) e.get("line_num"),
                        e -> mapToConvertedSaleroomSupplierBidLinePrice(e, currencyExchangeRate)));
    }

    private Map<String, BigDecimal> mapToConvertedSaleroomSupplierBidLinePrice(Map<String, Object> bidLine, BigDecimal exchangeRate) {
        BigDecimal bidPrice = (BigDecimal) bidLine.computeIfPresent("bid_price", (k, v) -> convertExchangeRate(v, exchangeRate));
        BigDecimal totalAmount = null;
        if (bidPrice != null) {
            BigDecimal quantity = ((BigDecimal) bidLine.get("quantity"));
            totalAmount = bidPrice.multiply(quantity).setScale(2, BigDecimal.ROUND_DOWN);
        }
        Map<String, BigDecimal> pricePair = new HashMap<>();
        pricePair.put("bid_price", bidPrice);
        pricePair.put("total_amount", totalAmount);
        return pricePair;
    }

    @Override
    public Map<Integer, BigDecimal> findAllSaleroomBids(Long negId, String currencyCode) {
        BigDecimal exchangeRate = exchangeRateService.findCurrentExchangeRate(currencyCode);
        String funcCurr = conf.getFunctionalCurrency();
        List<Map<String, Object>> res = daoutil.queryForMapList(this, "get_all_salesroom_bids", funcCurr, negId);
        return res.stream()
                .collect(toMap(e -> (Integer) e.get("line_num"),
                        e -> convertExchangeRate(e.get("best_price"), exchangeRate)));
    }

    private BigDecimal convertExchangeRate(Object price, BigDecimal exchangeRate) {
        return ((BigDecimal) price).divide(exchangeRate, 2, BigDecimal.ROUND_DOWN);
    }

    @Override
    @SpringTransactional
    public void resetAwardStatuses(Long userId, Long negId) {
        logger.debug("reset award statuses: negId={}", negId);
        Object[] values = {userId, negId};
        daoutil.dml(this, "reset_award_statuses", values);
    }
}