package com.bas.auction.neg.controller;

import com.bas.auction.auth.dto.User;
import com.bas.auction.bid.discount.dto.BidDiscount;
import com.bas.auction.bid.discount.service.BidDiscountService;
import com.bas.auction.bid.permission.dto.BidLinePermissions;
import com.bas.auction.bid.permission.service.BidPermissionsService;
import com.bas.auction.comment.dao.CommentDAO;
import com.bas.auction.core.RestControllerExceptionHandler;
import com.bas.auction.core.config.security.CurrentUser;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.neg.award.service.NegPublishResumeService;
import com.bas.auction.neg.award.service.NegResumeReportService;
import com.bas.auction.neg.dao.NegLineDAO;
import com.bas.auction.neg.dao.NegotiationDAO;
import com.bas.auction.neg.draft.service.NegDraftService;
import com.bas.auction.neg.dto.Negotiation;
import com.bas.auction.neg.publish.service.NegPublishService;
import com.bas.auction.neg.setting.service.NegDiscountService;
import com.bas.auction.neg.voting.service.NegVotingService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(path = "/negs", produces = APPLICATION_JSON_UTF8_VALUE)
public class NegotiationController extends RestControllerExceptionHandler {
    private final NegotiationDAO negDAO;
    private final NegLineDAO negLineDAO;
    private final NegDiscountService negDiscountService;
    private final BidDiscountService bidDiscountService;
    private final CommentDAO commentDAO;
    private final NegDraftService negDraftService;
    private final NegPublishService negPublishService;
    private final DocFileDAO docFileDAO;
    private final BidPermissionsService bidPermissionsService;
    private final NegVotingService negVotingService;
    private final NegResumeReportService negResumeReportService;
    private final NegPublishResumeService negPublishResumeService;

    @Autowired
    public NegotiationController(MessageDAO messageDAO, NegotiationDAO negDAO, NegLineDAO negLineDAO,
                                 NegDiscountService negDiscountService, BidDiscountService bidDiscountService,
                                 CommentDAO commentDAO, NegDraftService negDraftService,
                                 NegPublishService negPublishService, DocFileDAO docFileDAO,
                                 BidPermissionsService bidPermissionsService, NegVotingService negVotingService,
                                 NegResumeReportService negResumeReportService, NegPublishResumeService negPublishResumeService) {
        super(messageDAO);
        this.negDAO = negDAO;
        this.negLineDAO = negLineDAO;
        this.negDiscountService = negDiscountService;
        this.bidDiscountService = bidDiscountService;
        this.commentDAO = commentDAO;
        this.negDraftService = negDraftService;
        this.negPublishService = negPublishService;
        this.docFileDAO = docFileDAO;
        this.bidPermissionsService = bidPermissionsService;
        this.negVotingService = negVotingService;
        this.negResumeReportService = negResumeReportService;
        this.negPublishResumeService = negPublishResumeService;
    }

    @RequestMapping(path = "/summary", method = GET)
    public Map<String, Object> lastBidsSummary(@CurrentUser User user) {
        MDC.put("action", "last negs");
        Long customerId = user.getCustomerId();
        List<Map<String, Object>> lastPublishedNegs = negDAO.findLastPublishedNegs(customerId);
        List<Map<String, Object>> lastDraftNegs = negDAO.findLastDraftNegs(customerId);
        List<Map<String, Object>> lastAwardedFailedNegs = negDAO.findLastAwardedFailedNegs(customerId);
        Map<String, Object> res = new HashMap<>();
        res.put("lastPublishedNegs", lastPublishedNegs);
        res.put("lastDraftNegs", lastDraftNegs);
        res.put("lastAwardedFailedNegs", lastAwardedFailedNegs);
        return res;
    }

    @RequestMapping(path = "/{negId}", method = GET)
    public Negotiation findNeg(@PathVariable Long negId, @CurrentUser User user) {
        MDC.put("action", "find neg");
        Negotiation neg = negDAO.findCustomerNeg(user, negId);
        neg.setComments(commentDAO.findComments(negId));
        return neg;
    }

    @RequestMapping(path = "/{negId}/discounts/{bidId}", method = GET)
    public Map<String, Object> findNegDiscounts(@PathVariable Long negId, @PathVariable Long bidId, @CurrentUser User user) {
        MDC.put("action", "find neg discounts");
        Map<String, Object> res = new HashMap<>();
        res.put("neg_discounts", negDiscountService.findNegDiscounts(negId));
        res.put("bid_discounts", bidDiscountService.findBidOriginalDiscounts(bidId));
        return res;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.PUT, consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation create(@RequestBody Map<String, Object> params,
                              @CurrentUser User user) {
        MDC.put("action", "create neg");
        String title = (String) params.get("title");
        String negType = (String) params.get("neg_type");
        return negDraftService.create(user, title, negType);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(path = "/{negId}", method = DELETE, consumes = APPLICATION_JSON_UTF8_VALUE)
    public void delete(@PathVariable Long negId,
                       @CurrentUser User user) throws IOException {
        MDC.put("action", "delete neg");
        negDraftService.delete(user, negId);
    }

    @RequestMapping(path = "/{negId}", params = "save", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation save(@PathVariable Long negId,
                            @RequestBody Negotiation neg,
                            @CurrentUser User user) {
        MDC.put("action", "save neg");
        return negDraftService.update(user, neg);
    }

    @RequestMapping(path = "/{negId}", params = "delete_files", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation deleteFiles(@PathVariable Long negId,
                                   @RequestBody List<Long> fileIds,
                                   @CurrentUser User user) throws IOException {
        MDC.put("action", "delete neg files");
        return negDraftService.deleteFiles(user, negId, fileIds);
    }

    @RequestMapping(path = "/{negId}", params = "generate_publish_rep", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation generatePublishReport(@PathVariable Long negId,
                                             @RequestBody Negotiation neg,
                                             @CurrentUser User user) {
        MDC.put("action", "gen publish rep");
        neg = negDraftService.update(user, neg);
        negPublishService.generatePublishReport(user, neg.getNegId());
        neg.setNegFiles(docFileDAO.findByAttr(user, "neg_id", neg.getNegId()));
        neg.setHasNegPublishReport(true);
        return neg;
    }

    @RequestMapping(path = "/{negId}", params = "publish", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation publish(@PathVariable Long negId,
                               @CurrentUser User user) throws IOException {
        MDC.put("action", "publish neg");
        return negPublishService.publish(user, negId);
    }

    @RequestMapping(path = "/{negId}/permissions", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public void saveBidPermissions(@PathVariable Long negId,
                                   @RequestBody List<BidLinePermissions> permissions,
                                   @CurrentUser User user) {
        MDC.put("action", "save perms");
        bidPermissionsService.update(user, permissions);
    }

    @RequestMapping(path = "/{negId}/{lineNum}/{bidId}/discounts", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> saveBidDiscounts(@PathVariable Long negId,
                                                @PathVariable Integer lineNum,
                                                @PathVariable Long bidId,
                                                @RequestBody List<BidDiscount> discounts,
                                                @CurrentUser User user) {
        MDC.put("action", "save line discounts");
        bidDiscountService.correctBidLineDiscountsAndConfirm(user.getUserId(), bidId, lineNum, discounts);
        Map<String, Object> res = new HashMap<>();
        res.put("discounts_confirmed", negLineDAO.findIsDiscountConfirmed(bidId, lineNum));
        res.put("bid_line_discount", bidDiscountService.findBidLineDiscounts(bidId, lineNum));
        return res;
    }

    @RequestMapping(path = "/{negId}", params = "finish_voting", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation finishVoting(@PathVariable Long negId,
                                    @CurrentUser User user) throws IOException {
        MDC.put("action", "finish voting");
        return negVotingService.finishVoting(user, negId);
    }

    @RequestMapping(path = "/{negId}", params = "resume_voting", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation resumeVoting(@PathVariable Long negId,
                                    @CurrentUser User user) throws IOException {
        MDC.put("action", "resume voting");
        return negVotingService.resumeVoting(user, negId);
    }

    @RequestMapping(path = "/{negId}", params = "generate_final_rep", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation generateResumeReport(@PathVariable Long negId,
                                           @CurrentUser User user) throws IOException {
        MDC.put("action", "gen resume rep");
        return negResumeReportService.generateResumeReport(user, negId);
    }

    @RequestMapping(path = "/{negId}", params = "publish_resume", method = POST,
            consumes = APPLICATION_JSON_UTF8_VALUE)
    public Negotiation publishResume(@PathVariable Long negId,
                                     @CurrentUser User user) throws IOException {
        MDC.put("action", "publish resume");
        return negPublishResumeService.publishResume(user, negId);
    }
}