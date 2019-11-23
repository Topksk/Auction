package com.bas.auction.bid.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.dao.BidDAO;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.draft.service.BidDraftService;
import com.bas.auction.bid.dto.Bid;
import com.bas.auction.bid.publish.service.BidPublishService;
import com.bas.auction.bid.replace.service.BidReplaceService;
import com.bas.auction.bid.withdraw.service.BidWithdrawService;
import com.bas.auction.comment.dao.CommentDAO;
import com.bas.auction.core.AccessDeniedException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.setting.service.NegDiscountService;
import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.profile.customer.setting.dao.MdDiscountDAO;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/bids", produces = APPLICATION_JSON_UTF8_VALUE)
public class BidController extends RestControllerExceptionHandler {
    private final BidDAO bidDAO;
    private final Conf conf;
    private final BidDraftService bidDraftService;
    private final BidReplaceService bidReplaceService;
    private final BidWithdrawService bidWithdrawService;
    private final BidPublishService bidPublishService;
    private final NegotiationDAO negotiationDAO;
    private final CommentDAO commentDAO;
    private final BidDiscountService bidDiscountDAO;
    private final NegDiscountService negDiscountService;
    private final PlanColDAO planColDAO;

    @Autowired
    public BidController(MessageDAO messageDAO, BidDAO bidDAO, Conf conf, BidDraftService bidDraftService, BidReplaceService bidReplaceService, BidWithdrawService bidWithdrawService, BidPublishService bidPublishService, NegotiationDAO negotiationDAO, CommentDAO commentDAO, BidDiscountService bidDiscountDAO, MdDiscountDAO mdDiscountDAO, NegDiscountService negDiscountService, PlanColDAO planColDAO) {
        super(messageDAO);
        this.bidDAO = bidDAO;
        this.conf = conf;
        this.bidDraftService = bidDraftService;
        this.bidReplaceService = bidReplaceService;
        this.bidWithdrawService = bidWithdrawService;
        this.bidPublishService = bidPublishService;
        this.negotiationDAO = negotiationDAO;
        this.commentDAO = commentDAO;
        this.bidDiscountDAO = bidDiscountDAO;
        this.negDiscountService = negDiscountService;
        this.planColDAO = planColDAO;
    }

    @RequestMapping(path = "/summary", method = GET)
    public Map<String, Object> lastBidsSummary(@CurrentUser User user) {
        MDC.put("action", "last bids");
        Long supplierId = user.getSupplierId();
        List<Map<String, Object>> lastActiveBids = bidDAO.findLastActiveBids(supplierId);
        List<Map<String, Object>> lastDraftBids = bidDAO.findLastDraftBids(supplierId);
        List<Map<String, Object>> lastAwardedBids = bidDAO.findLastAwardedBids(supplierId);
        List<Map<String, Object>> lastRejectedBids = bidDAO.findLastRejectedBids(supplierId);
        List<Map<String, Object>> tender2Stage2Negs = bidDAO.findPublishedTender2Stage2Negs(supplierId);
        Map<String, Object> res = new HashMap<>();
        res.put("lastActiveBids", lastActiveBids);
        res.put("lastDraftBids", lastDraftBids);
        res.put("lastAwardedBids", lastAwardedBids);
        res.put("lastRejectedBids", lastRejectedBids);
        res.put("tender2Stage2Negs", tender2Stage2Negs);
        return res;
    }

    @RequestMapping(path = "/{bidId}", method = GET)
    public Bid findBid(@PathVariable Long bidId,
                       @CurrentUser User user) {
        MDC.put("action", "find bid");
        return bidDAO.findById(user, bidId);
    }

    @RequestMapping(path = "/negs/{negId}", method = GET)
    public Negotiation findNotDraftNeg(@PathVariable Long negId,
                                       @CurrentUser User user) {
        MDC.put("action", "find not draft neg");
        Negotiation neg = negotiationDAO.findNotDraftNeg(user, negId);
        if (neg == null)
            throw new AccessDeniedException();
        Long supplierActiveBidId = bidDAO.findSupplierActiveBidId(user.getSupplierId(), negId);
        neg.setCurrentSupplierBidId(supplierActiveBidId);
        neg.setComments(commentDAO.findComments(negId));
        return neg;
    }

    @RequestMapping(path = "/{bidId}/discounts", method = GET)
    public Map<String, Object> findBidDiscounts(@PathVariable Long bidId,
                                                @CurrentUser User user) {
        MDC.put("action", "find bid discounts");
        Map<String, Object> res = new HashMap<>();
        Long negId = bidDAO.findBidNegId(bidId);
        res.put("neg_discounts", negDiscountService.findNegDiscounts(negId));
        res.put("bid_discounts", bidDiscountDAO.findBidDiscounts(bidId));
        return res;
    }

    @RequestMapping(path = "/negs/lineCols/{settingId}", method = GET)
    public List<PlanCol> findPlanColumns(@PathVariable Long settingId,
                                         @CurrentUser User user) {
        MDC.put("action", "find plan cols");
        return planColDAO.findSettingDisplayedPlanColList(settingId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid create(@RequestBody Map<String, Object> params,
                      @CurrentUser User user) {
        MDC.put("action", "create bid");
        long negId = ((Number) params.get("neg_id")).longValue();
        String currencyCode = (String) params.get("currency");
        if (currencyCode == null)
            currencyCode = conf.getFunctionalCurrency();
        return bidDraftService.create(user, negId, currencyCode);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "/{bidId}", method = DELETE, consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public void delete(@PathVariable Long bidId,
                       @CurrentUser User user) throws IOException {
        MDC.put("action", "delete bid");
        bidDraftService.delete(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "replace", method = POST,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public Bid replace(@PathVariable Long bidId,
                       @CurrentUser User user) throws IOException {
        MDC.put("action", "replace bid");
        return bidReplaceService.replace(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "withdraw", method = POST,
            consumes = APPLICATION_FORM_URLENCODED_VALUE)
    public Bid withdraw(@PathVariable Long bidId,
                        @CurrentUser User user) {
        MDC.put("action", "withdraw bid");
        return bidWithdrawService.withdraw(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "delete_files", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid deleteFiles(@PathVariable Long bidId,
                           @RequestBody List<Long> fileIds,
                           @CurrentUser User user) throws IOException {
        MDC.put("action", "delete bid files");
        return bidDraftService.deleteFiles(user, bidId, fileIds);
    }

    @RequestMapping(path = "/{bidId}", params = "update_currency", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid updateCurrency(@PathVariable Long bidId,
                              @RequestBody Map<String, String> currency,
                              @CurrentUser User user) {
        MDC.put("action", "update bid currency");
        return bidDraftService.updateCurrency(user, bidId, currency.get("currency_code"));
    }

    @RequestMapping(path = "/{bidId}/discounts", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> saveDiscounts(@PathVariable Long bidId,
                                               @RequestBody List<BidDiscount> discounts,
                                               @CurrentUser User user) {
        MDC.put("action", "save bid discounts");
        bidDiscountDAO.update(user.getUserId(), bidId, discounts);
        Long negId = bidDAO.findBidNegId(bidId);
        Map<String, Object> res = new HashMap<>();
        res.put("neg_discounts", negDiscountService.findNegDiscounts(negId));
        res.put("bid_discounts", bidDiscountDAO.findBidDiscounts(bidId));
        return res;
    }

    @RequestMapping(path = "/{bidId}", params = "save", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid save(@PathVariable Long bidId,
                    @RequestBody Bid bid,
                    @CurrentUser User user) {
        MDC.put("action", "save bid");
        return bidDraftService.update(user, bid);
    }

    @RequestMapping(path = "/{bidId}", params = "generate_bid_rep", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid generateBidReport(@PathVariable Long bidId,
                                 @RequestBody Bid bid,
                                 @CurrentUser User user) throws Exception {
        MDC.put("action", "gen bid rep");
        bidDraftService.updateWithoutIndexing(user, bid);
        return bidPublishService.generateBidReport(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "generate_bid_appl", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid generateBidParticipationAppl(@PathVariable Long bidId,
                                            @RequestBody Bid bid,
                                            @CurrentUser User user) throws Exception {
        MDC.put("action", "gen bid appl");
        bidDraftService.updateWithoutIndexing(user, bid);
        return bidPublishService.generateBidParticipationAppl(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "generate_bid_rep_appl", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid generateBidReportAndParticipationAppl(@PathVariable Long bidId,
                                                     @RequestBody Bid bid,
                                                     @CurrentUser User user) throws Exception {
        MDC.put("action", "gen bid rep appl");
        bidDraftService.updateWithoutIndexing(user, bid);
        return bidPublishService.generateBidReportAndParticipationAppl(user, bidId);
    }

    @RequestMapping(path = "/{bidId}", params = "send", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Bid send(@PathVariable Long bidId,
                    @RequestBody Bid bid,
                    @CurrentUser User user) throws Exception {
        MDC.put("action", "send bid");
        return bidPublishService.send(user, bidId);
    }

}