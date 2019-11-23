package com.bas.auction.neg.dto;

import com.bas.auction.bid.dto.Bid;
import com.bas.auction.comment.dto.Comment;
import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromClient;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.bas.auction.docfiles.dto.DocFile;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Negotiation extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -8832424373200038843L;


    public enum NegType {
        RFQ, AUCTION, TENDER, TENDER2
    }
    @SerializedName("recid")
    private long negId;

    private Long parentNegId;
    private String docNumber;
    private String title;
    private String titleKz;
    private String description;
    private NegType negType;
    private String[] category;
    private String negStatus;
    private Date openDate;
    private Date closeDate;
    private Date actualCloseDate;
    @ExcludeFromClient
    private Date unlockDate;
    private Date publishDate;
    private Date awardDate;
    private Integer minBidLimitDays;
    private Boolean dumpingControlEnabled;
    private String goodDumpingCalcMethod;
    private BigDecimal goodDumpingThreshold;
    private String workDumpingCalcMethod;
    private BigDecimal workDumpingThreshold;
    private String serviceDumpingCalcMethod;
    private BigDecimal serviceDumpingThreshold;
    private Date cancelDate;
    private String cancelDesc;
    private long settingId;
    private long customerId;
    @ExcludeFromSearchIndex
    private Long customerRulesFileId;
    private Integer stage;
    private List<NegTeam> negTeam;
    private List<NegLine> negLines;
    private List<DocFile> negFiles;
    @ExcludeFromSearchIndex
    private List<Long> newLines;
    @ExcludeFromSearchIndex
    private List<Long> delTeam;
    @ExcludeFromSearchIndex
    private List<Long> delLines;
    @ExcludeFromSearchIndex
    private Boolean hasNegPublishReport;
    @ExcludeFromSearchIndex
    private Boolean hasNegResumeReport;
    @ExcludeFromSearchIndex
    private List<Bid> bids;
    @ExcludeFromSearchIndex
    private Long currentSupplierBidId;
    @ExcludeFromClient
    @SerializedName("bids")
    private List<Map<String, Long>> bidIds;
    private BigDecimal auctionBidStep;
    private String auctionBidStepType;
    private List<Comment> comments;
    public long getNegId() {
        return negId;
    }

    public void setNegId(long negId) {
        this.negId = negId;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKz() {
        return titleKz;
    }

    public void setTitleKz(String titleKz) {
        this.titleKz = titleKz;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NegType getNegType() {
        return negType;
    }

    public void setNegType(NegType negType) {
        this.negType = negType;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String getNegStatus() {
        return negStatus;
    }

    public boolean isVoting() {
        return "VOTING".equals(negStatus);
    }

    public boolean isVotingFinished() {
        return "VOTING_FINISHED".equals(negStatus);
    }

    public boolean isFailed() {
        return "NEG_FAILED".equals(negStatus);
    }

    public boolean isAwarded() {
        return "AWARDED".equals(negStatus);
    }

    public boolean isPublished() {
        Date now = new Date();
        return "PUBLISHED".equals(negStatus) &&
                openDate.compareTo(now) <= 0 &&
                closeDate.compareTo(now) > 0;
    }

    public boolean isFirstStageFinished() {
        return "FIRST_STAGE_FINISHED".equals(negStatus);
    }

    public void setNegStatus(String negStatus) {
        this.negStatus = negStatus;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public Date getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(Date unlockDate) {
        this.unlockDate = unlockDate;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(Date awardDate) {
        this.awardDate = awardDate;
    }

    public Date getCancelDate() {
        return cancelDate;
    }

    public void setCancelDate(Date cancelDate) {
        this.cancelDate = cancelDate;
    }

    public String getCancelDesc() {
        return cancelDesc;
    }

    public void setCancelDesc(String cancelDesc) {
        this.cancelDesc = cancelDesc;
    }

    public List<NegTeam> getNegTeam() {
        return negTeam;
    }

    public void setNegTeam(List<NegTeam> negTeam) {
        this.negTeam = negTeam;
    }

    public List<NegLine> getNegLines() {
        return negLines;
    }

    public void setNegLines(List<NegLine> negLines) {
        this.negLines = negLines;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public List<DocFile> getNegFiles() {
        return negFiles;
    }

    public void setNegFiles(List<DocFile> negFiles) {
        this.negFiles = negFiles;
    }

    public boolean isHasNegPublishReport() {
        return hasNegPublishReport;
    }

    public void setHasNegPublishReport(boolean hasNegPublishReport) {
        this.hasNegPublishReport = hasNegPublishReport;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public Date getActualCloseDate() {
        return actualCloseDate;
    }

    public void setActualCloseDate(Date actualCloseDate) {
        this.actualCloseDate = actualCloseDate;
    }

    public List<Map<String, Long>> getBidIds() {
        return bidIds;
    }

    public void setBidIds(List<Map<String, Long>> bidIds) {
        this.bidIds = bidIds;
    }

    public Boolean isDumpingControlEnabled() {
        return dumpingControlEnabled;
    }

    public void setDumpingControlEnabled(Boolean dumpingControlEnabled) {
        this.dumpingControlEnabled = dumpingControlEnabled;
    }

    public String getGoodDumpingCalcMethod() {
        return goodDumpingCalcMethod;
    }

    public void setGoodDumpingCalcMethod(String goodDumpingCalcMethod) {
        this.goodDumpingCalcMethod = goodDumpingCalcMethod;
    }

    public BigDecimal getGoodDumpingThreshold() {
        return goodDumpingThreshold;
    }

    public void setGoodDumpingThreshold(BigDecimal goodDumpingThreshold) {
        this.goodDumpingThreshold = goodDumpingThreshold;
    }

    public String getWorkDumpingCalcMethod() {
        return workDumpingCalcMethod;
    }

    public void setWorkDumpingCalcMethod(String workDumpingCalcMethod) {
        this.workDumpingCalcMethod = workDumpingCalcMethod;
    }

    public BigDecimal getWorkDumpingThreshold() {
        return workDumpingThreshold;
    }

    public void setWorkDumpingThreshold(BigDecimal workDumpingThreshold) {
        this.workDumpingThreshold = workDumpingThreshold;
    }

    public String getServiceDumpingCalcMethod() {
        return serviceDumpingCalcMethod;
    }

    public void setServiceDumpingCalcMethod(String serviceDumpingCalcMethod) {
        this.serviceDumpingCalcMethod = serviceDumpingCalcMethod;
    }

    public BigDecimal getServiceDumpingThreshold() {
        return serviceDumpingThreshold;
    }

    public void setServiceDumpingThreshold(BigDecimal serviceDumpingThreshold) {
        this.serviceDumpingThreshold = serviceDumpingThreshold;
    }

    public Integer getMinBidLimitDays() {
        return minBidLimitDays;
    }

    public void setMinBidLimitDays(Integer minBidLimitDays) {
        this.minBidLimitDays = minBidLimitDays;
    }

    public Boolean getHasNegResumeReport() {
        return hasNegResumeReport;
    }

    public void setHasNegResumeReport(Boolean hasNegResumeReport) {
        this.hasNegResumeReport = hasNegResumeReport;
    }

    public long getSettingId() {
        return settingId;
    }

    public void setSettingId(long settingId) {
        this.settingId = settingId;
    }

    public BigDecimal getAuctionBidStep() {
        return auctionBidStep;
    }

    public void setAuctionBidStep(BigDecimal auctionBidStep) {
        this.auctionBidStep = auctionBidStep;
    }

    public String getAuctionBidStepType() {
        return auctionBidStepType;
    }

    public void setAuctionBidStepType(String auctionBidStepType) {
        this.auctionBidStepType = auctionBidStepType;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Long getParentNegId() {
        return parentNegId;
    }

    public void setParentNegId(Long parentNegId) {
        this.parentNegId = parentNegId;
    }

    public List<Long> getNewLines() {
        return newLines;
    }

    public void setNewLines(List<Long> newLines) {
        this.newLines = newLines;
    }

    public List<Long> getDelTeam() {
        return delTeam;
    }

    public void setDelTeam(List<Long> delTeam) {
        this.delTeam = delTeam;
    }

    public List<Long> getDelLines() {
        return delLines;
    }

    public void setDelLines(List<Long> delLines) {
        this.delLines = delLines;
    }

    public boolean isTender() {
        return negType == NegType.TENDER;
    }

    public boolean isTender2() {
        return negType == NegType.TENDER2;
    }

    public boolean isTender2Stage1() {
        return isTender2() && stage == 1;
    }

    public boolean isTender2Stage2() {
        return isTender2() && stage == 2;
    }

    public Long getCustomerRulesFileId() {
        return customerRulesFileId;
    }

    public void setCustomerRulesFileId(Long customerRulesFileId) {
        this.customerRulesFileId = customerRulesFileId;
    }

    public Long getCurrentSupplierBidId() {
        return currentSupplierBidId;
    }

    public void setCurrentSupplierBidId(Long currentSupplierBidId) {
        this.currentSupplierBidId = currentSupplierBidId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (negId ^ (negId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Negotiation other = (Negotiation) obj;
        return negId == other.negId;
    }
}
