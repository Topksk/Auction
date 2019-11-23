package com.bas.auction.plans.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.dao.SqlAware;
import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import org.apache.poi.ss.usermodel.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

class PlanImportHelper implements SqlAware {
    private final DaoJdbcUtil daoutil;
    private final List<PlanCol> cols;
    private final MessageDAO messages;
    private final Long customerId;
    private final long settingId;

    private CreationHelper factory;
    private Drawing drawing;
    private CellStyle style;
    private Map<String, Boolean> itemCodes = new HashMap<>();
    private Map<String, Boolean> uomCodes = new HashMap<>();
    private int currYear;
    private Map<String, String> purchTypes;
    private Map<String, String> purchasePriorities;
    private String itemCodeListType;
    private Map<String, String> purchMethods;
    private Map<String, String> regions;
    private String wrongPurchMethodMsg;
    private String good;
    private List<String> incoterms2010;

    public PlanImportHelper(DaoJdbcUtil daoutil, CustomerSetting set, List<PlanCol> cols, MessageDAO messages,
                            Long customerId) {
        this.daoutil = daoutil;
        this.cols = cols.stream()
                .filter(PlanCol::getDisplayInTemplate)
                .collect(Collectors.toList());
        this.messages = messages;
        this.customerId = customerId;
        this.itemCodeListType = set.getItemCodeListType();
        this.settingId = set.getSettingId();
        this.currYear = Calendar.getInstance().get(Calendar.YEAR);
        this.incoterms2010 = Arrays.asList(messages.get("INCOTERMS2010").toUpperCase().split(","));
        initRegions();
        initPurchasePriority();
        initPurchTypes();
        initPurchMethods(set);
    }

    @Override
    public String getSqlPath() {
        return "plans";
    }

    public void addComment(Row row, Cell cell, String code) {
        cell.removeCellComment();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 4);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 3);
        Comment comment = drawing.createCellComment(anchor);
        String msg;
        if ("WRONG_PURCHASE_METHOD".equals(code))
            msg = wrongPurchMethodMsg;
        else
            msg = messages.get(code);
        RichTextString str = factory.createRichTextString(msg);
        comment.setString(str);
        cell.setCellComment(comment);
        cell.setCellStyle(style);
    }

    public void initWorkbookValidation(Workbook wb) {
        factory = wb.getCreationHelper();
        drawing = wb.getSheetAt(0).createDrawingPatriarch();
        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
    }

    private void initPurchTypes() {
        good = messages.get("GOOD").toLowerCase();
        purchTypes = new HashMap<>();
        purchTypes.put(good, "GOOD");
        purchTypes.put(messages.get("WORK").toLowerCase(), "WORK");
        purchTypes.put(messages.get("SERVICE").toLowerCase(), "SERVICE");
    }

    private void initPurchMethods(CustomerSetting set) {
        purchMethods = new HashMap<>();
        if (set.isRfqEnabled())
            purchMethods.put(messages.get("RFQ_SHORT").toLowerCase(), "RFQ");
        if (set.isAuctionEnabled())
            purchMethods.put(messages.get("AUCTION_SHORT").toLowerCase(), "AUCTION");
        if (set.isTenderEnabled())
            purchMethods.put(messages.get("TENDER_SHORT").toLowerCase(), "TENDER");
        if (set.isTender2Enabled())
            purchMethods.put(messages.get("TENDER2_SHORT").toLowerCase(), "TENDER2");
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = purchMethods.keySet().iterator();
        if (!iter.hasNext())
            return;
        while (true) {
            sb.append(iter.next());
            if (!iter.hasNext()) {
                Map<String, String> params = Collections.singletonMap("purch_methods", sb.toString());
                wrongPurchMethodMsg = messages.get("WRONG_PURCHASE_METHOD", params);
                return;
            }
            sb.append(",");
        }
    }

    private void initRegions() {
        List<Map<String, String>> list = daoutil.queryForTypedMapList(this, "region_codes");
        regions = list.stream()
                .collect(toMap(e -> e.get("meaning"), e -> e.get("lookup_code")));
    }

    private void initPurchasePriority() {
        List<Map<String, String>> list = daoutil.queryForTypedMapList(this, "purchase_priorities");
        purchasePriorities = list.stream()
                .collect(toMap(e -> e.get("meaning"), e -> e.get("lookup_code")));
    }

    public boolean isGood(Map<String, PlanField> fields) {
        Cell cell = fields.get("purchase_type").cell;
        String val = getCellTextVal(cell);
        return good.equalsIgnoreCase(val);
    }

    public boolean uniquePlanNumber(String planNum, int finYear) {
        return !daoutil.exists(this, "plan_number_exists", planNum, customerId, finYear);
    }

    public boolean validItemCode(String code) {
        Boolean res = itemCodes.get(code);
        if (res != null)
            return res;
        res = daoutil.exists(this, "item_code_exists", itemCodeListType, code);
        itemCodes.put(code, res);
        return res;
    }

    public boolean validUomCode(String code) {
        Boolean res = uomCodes.get(code);
        if (res != null)
            return res;
        res = daoutil.exists(this, "uom_code_exists", code);
        uomCodes.put(code, res);
        return res;
    }

    public boolean validPurchType(String val) {
        return purchTypes.containsKey(val.toLowerCase());
    }

    public boolean validPurchMethod(String val) {
        return purchMethods.containsKey(val.toLowerCase());
    }

    public boolean validIncoterms2010(String val) {
        return incoterms2010.contains(val.toUpperCase());
    }

    public boolean validRegion(String val) {
        return regions.containsKey(val.toLowerCase());
    }

    public boolean validPurchasePriority(String val) {
        return purchasePriorities.containsKey(val.toLowerCase());
    }

    public int getFinYear(Map<String, PlanField> fields) {
        int fyear = 0;
        Cell fcell = fields.get("financial_year").cell;
        if (fcell != null && fcell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            Double fval = (Double) getCellVal(fcell);
            if (fval != null) {
                fyear = fval.intValue();
            }
        }
        return fyear;
    }

    public String getCellTextVal(Cell cell) {
        if (cell == null)
            return null;
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.getRichStringCellValue().getString();
    }

    public Object getCellVal(Cell cell) {
        if (cell == null)
            return null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            default:
                return null;
        }
    }

    public List<PlanCol> getCols() {
        return cols;
    }

    public int getCurrYear() {
        return currYear;
    }

    public Map<String, String> getPurchMethods() {
        return purchMethods;
    }

    public Map<String, String> getRegions() {
        return regions;
    }

    public Map<String, String> getPurchasePriorities() {
        return purchasePriorities;
    }

    public Map<String, String> getPurchTypes() {
        return purchTypes;
    }

    public long getSettingId() {
        return settingId;
    }

    public String getItemCodeListType() {
        return itemCodeListType;
    }
}