package com.bas.auction.profile.customer.setting.dto;

import com.bas.auction.plans.dto.PlanCol;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CustomerSetting {

	@SerializedName("recid")
	private long settingId;
	private String name;
	private boolean main;
	private boolean rfqEnabled;
	private boolean rfqForeignCurrencyControl;
	private boolean tenderForeignCurrencyControl;
	private String rfqAwardMethod;
	private boolean tenderEnabled;
	private String itemCodeListType;
	private boolean plansEnabled;
	private long customerId;
	private boolean auctionEnabled;
	private String auctionAwardMethod;
	private boolean auctionForeignCurrencyControl;
	private Integer auctionDuration;
	private Integer auctionExtTimeLeft;
	private Integer auctionExtDuration;
	private Integer auctionExtNumber;
	private boolean tender2Enabled;
	private boolean tender2ForeignCurrencyControl;
	private List<PlanCol> planCols;
	private List<MdRequirement> rfqReqs;
	private List<MdRequirement> auctionReqs;
	private List<MdRequirement> tenderReqs;
	private List<MdDiscount> tenderDiscounts;
	private List<MdRequirement> tender2Reqs;
	private List<MdDiscount> tender2Discounts;
	private List<Long> delDiscounts;
	private Boolean integrationSendAward;

	public long getSettingId() {
		return settingId;
	}

	public void setSettingId(long settingId) {
		this.settingId = settingId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public boolean isRfqEnabled() {
		return rfqEnabled;
	}

	public void setRfqEnabled(boolean rfqEnabled) {
		this.rfqEnabled = rfqEnabled;
	}

	public String getItemCodeListType() {
		return itemCodeListType;
	}

	public void setItemCodeListType(String itemCodeListType) {
		this.itemCodeListType = itemCodeListType;
	}

	public boolean isPlansEnabled() {
		return plansEnabled;
	}

	public void setPlansEnabled(boolean plansEnabled) {
		this.plansEnabled = plansEnabled;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getRfqAwardMethod() {
		return rfqAwardMethod;
	}

	public void setRfqAwardMethod(String rfqAwardMethod) {
		this.rfqAwardMethod = rfqAwardMethod;
	}

	public List<MdRequirement> getRfqReqs() {
		return rfqReqs;
	}

	public void setRfqReqs(List<MdRequirement> rfqReqs) {
		this.rfqReqs = rfqReqs;
	}

	public boolean isRfqForeignCurrencyControl() {
		return rfqForeignCurrencyControl;
	}

	public void setRfqForeignCurrencyControl(boolean rfqForeignCurrencyControl) {
		this.rfqForeignCurrencyControl = rfqForeignCurrencyControl;
	}

	public boolean isTenderEnabled() {
		return tenderEnabled;
	}

	public void setTenderEnabled(boolean tenderEnabled) {
		this.tenderEnabled = tenderEnabled;
	}

	public List<MdRequirement> getTenderReqs() {
		return tenderReqs;
	}

	public void setTenderReqs(List<MdRequirement> tenderReqs) {
		this.tenderReqs = tenderReqs;
	}

	public List<MdRequirement> getTender2Reqs() {
		return tender2Reqs;
	}

	public void setTender2Reqs(List<MdRequirement> tender2Reqs) {
		this.tender2Reqs = tender2Reqs;
	}

	public List<MdDiscount> getTenderDiscounts() {
		return tenderDiscounts;
	}

	public void setTenderDiscounts(List<MdDiscount> tenderDiscounts) {
		this.tenderDiscounts = tenderDiscounts;
	}

	public List<MdDiscount> getTender2Discounts() {
		return tender2Discounts;
	}

	public void setTender2Discounts(List<MdDiscount> tender2Discounts) {
		this.tender2Discounts = tender2Discounts;
	}

	public boolean isTenderForeignCurrencyControl() {
		return tenderForeignCurrencyControl;
	}

	public void setTenderForeignCurrencyControl(boolean tenderForeignCurrencyControl) {
		this.tenderForeignCurrencyControl = tenderForeignCurrencyControl;
	}

	public List<PlanCol> getPlanCols() {
		return planCols;
	}

	public void setPlanCols(List<PlanCol> planCols) {
		this.planCols = planCols;
	}

	public boolean isAuctionEnabled() {
		return auctionEnabled;
	}

	public void setAuctionEnabled(boolean auctionEnabled) {
		this.auctionEnabled = auctionEnabled;
	}

	public String getAuctionAwardMethod() {
		return auctionAwardMethod;
	}

	public void setAuctionAwardMethod(String auctionAwardMethod) {
		this.auctionAwardMethod = auctionAwardMethod;
	}

	public boolean isAuctionForeignCurrencyControl() {
		return auctionForeignCurrencyControl;
	}

	public void setAuctionForeignCurrencyControl(boolean auctionForeignCurrencyControl) {
		this.auctionForeignCurrencyControl = auctionForeignCurrencyControl;
	}

	public Integer getAuctionDuration() {
		return auctionDuration;
	}

	public void setAuctionDuration(Integer auctionDuration) {
		this.auctionDuration = auctionDuration;
	}

	public Integer getAuctionExtTimeLeft() {
		return auctionExtTimeLeft;
	}

	public void setAuctionExtTimeLeft(Integer auctionExtTimeLeft) {
		this.auctionExtTimeLeft = auctionExtTimeLeft;
	}

	public Integer getAuctionExtDuration() {
		return auctionExtDuration;
	}

	public void setAuctionExtDuration(Integer auctionExtDuration) {
		this.auctionExtDuration = auctionExtDuration;
	}

	public Integer getAuctionExtNumber() {
		return auctionExtNumber;
	}

	public void setAuctionExtNumber(Integer auctionExtNumber) {
		this.auctionExtNumber = auctionExtNumber;
	}

	public List<MdRequirement> getAuctionReqs() {
		return auctionReqs;
	}

	public void setAuctionReqs(List<MdRequirement> auctionReqs) {
		this.auctionReqs = auctionReqs;
	}

	public void setTender2ForeignCurrencyControl(boolean tender2ForeignCurrencyControl) {
		this.tender2ForeignCurrencyControl = tender2ForeignCurrencyControl;
	}

	public boolean isTender2ForeignCurrencyControl() {
		return tender2ForeignCurrencyControl;
	}

	public void setTender2Enabled(boolean tender2Enabled) {
		this.tender2Enabled = tender2Enabled;
	}

	public boolean isTender2Enabled() {
		return tender2Enabled;
	}

	public List<Long> getDelDiscounts() {
		return delDiscounts;
	}

	public void setDelDiscounts(List<Long> delDiscounts) {
		this.delDiscounts = delDiscounts;
	}

	public Boolean isIntegrationSendAward() {
		return integrationSendAward;
	}

	public void setIntegrationSendAward(Boolean integrationSendAward) {
		this.integrationSendAward = integrationSendAward;
	}
}
