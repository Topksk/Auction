package com.bas.auction.plans.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.dao.SqlAware;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.core.utils.Utils;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.plans.dao.PlanDAO;
import com.bas.auction.plans.dao.PlanImporter;
import com.bas.auction.plans.dto.Plan;
import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.plans.dto.PlanImport;
import com.bas.auction.plans.service.PlanService;
import com.bas.auction.profile.customer.setting.dto.CustomerSetting;
import com.bas.auction.profile.customer.setting.dao.CustomerSettingDAO;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanImporterImpl implements PlanImporter, SqlAware {
    static {
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    }

    private final static Logger logger = LoggerFactory.getLogger(PlanImporterImpl.class);
    private final DaoJdbcUtil daoutil;
    private final DocFileDAO docFileDAO;
    private final PlanColDAO planColDAO;
    private final CustomerSettingDAO customerSettingsDAO;
    private final PlanDAO planDAO;
    private final PlanService planService;
    private final MessageDAO messages;
    private final Utils util;

    @Autowired
    public PlanImporterImpl(DaoJdbcUtil daoutil, DocFileDAO docFileDAO, PlanColDAO planColDAO, PlanDAO planDAO, PlanService planService,
                            CustomerSettingDAO customerSettingsDAO, MessageDAO messages, Utils util) {
        this.daoutil = daoutil;
        this.docFileDAO = docFileDAO;
        this.planColDAO = planColDAO;
        this.planDAO = planDAO;
        this.planService = planService;
        this.customerSettingsDAO = customerSettingsDAO;
        this.messages = messages;
        this.util = util;
    }

    @Override
    public String getSqlPath() {
        return "plans";
    }

    @Override
    @SpringTransactional
    public void parseImport(PlanImport pi) throws Exception {
        CustomerSetting set = customerSettingsDAO.findMainWithoutDetails(pi.user.getCustomerId());
        List<PlanCol> cols = planColDAO.findSettingPlanColList(set.getSettingId());
        PlanImportHelper pih = new PlanImportHelper(daoutil, set, cols, messages, pi.user.getCustomerId());
        if (validate(pi, pih)) {
            importPlan(pi, pih);
            updateFileStatus(pi, "IMPORTED", false);
        } else {
            updateFileStatus(pi, "ERROR", true);
        }
    }

    private void updateFileStatus(PlanImport pi, String status, boolean error) throws IOException {
        logger.debug("Updating plan import status: {}. Status {}", pi.id, status);
        Long logId = null;
        if (error) {
            logger.debug("Log file exists: {}", Files.exists(pi.getLog()));
            String fileName = "log_" + pi.fileId + ".xlsx";
            logId = docFileDAO.create(fileName, pi.user.getUserId(), false, pi.getLog(), null);
            logger.debug("Log file id: {}", logId);
        }
        if (Files.deleteIfExists(pi.getLog()))
            logger.debug("Log file deleted");
        Object[] values = {status, logId, pi.user.getUserId(), pi.id};
        daoutil.dml(this, "update_import_file", values);
        logger.debug("Plan import status changed");
    }

    private Map<String, Method> findPlanProperties() {
        Map<String, Method> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Plan.class, Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                String name = util.camelCaseToUnderscore(pd.getName());
                map.put(name, pd.getWriteMethod());
            }
        } catch (IntrospectionException e) {
            logger.error("Error importing plan", e);
        }
        return map;
    }

    private void importPlan(PlanImport pi, PlanImportHelper pih) throws InvalidFormatException, IOException {
        logger.debug("Start importing plan file into database: {}", pi.id);
        int colCnt = pih.getCols().size();
        int count = 0;
        try (Workbook wb = WorkbookFactory.create(OPCPackage.open(pi.path.toFile(), PackageAccess.READ))) {
            Sheet sheet = wb.getSheetAt(0);
            List<Plan> plans = new ArrayList<>();
            Map<String, Method> fields = findPlanProperties();
            for (Row row : sheet) {
                if (row.getRowNum() < 2)
                    continue;
                Plan plan = new Plan();
                plan.setCustomerId(pi.user.getCustomerId());
                plan.setItemCodeListType(pih.getItemCodeListType());
                plan.setSettingId(pih.getSettingId());
                for (int i = 0; i < colCnt; i++) {
                    Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
                    if (cell == null)
                        continue;
                    PlanCol col = pih.getCols().get(i);
                    Object val;
                    String colName = col.getColName();
                    switch (col.getColType()) {
                        case "text":
                            val = pih.getCellTextVal(cell);
                            if (val != null) {
                                if ("purchase_method".equals(colName)) {
                                    val = ((String) val).toLowerCase();
                                    val = pih.getPurchMethods().get(val);
                                } else if ("purchase_type".equals(colName)) {
                                    val = ((String) val).toLowerCase();
                                    val = pih.getPurchTypes().get(val);
                                } else if ("shipping_region".equals(colName)) {
                                    val = ((String) val).toLowerCase();
                                    val = pih.getRegions().get(val);
                                } else if("purchase_priority".equals(colName)) {
                                    val = ((String) val).toLowerCase();
                                    val = pih.getPurchasePriorities().get(val);
                                }
                            }
                            break;
                        case "integer":
                            val = pih.getCellTextVal(cell);
                            val = Integer.valueOf(val.toString());
                            break;
                        case "bigint":
                            val = pih.getCellTextVal(cell);
                            val = Long.valueOf(val.toString());
                            break;
                        case "numeric":
                            val = pih.getCellVal(cell);
                            val = BigDecimal.valueOf((Double) val);
                            break;
                        default:
                            continue;
                    }
                    fields.get(colName).invoke(plan, val);
                }
                plans.add(planService.createForImport(pi.user, plan));
                count++;
                if (plans.size() >= 100) {
                    planDAO.indexPlansSync(plans);
                    plans.clear();
                }
            }
            if (plans.size() > 0) {
                planDAO.indexPlansSync(plans);
            }
        } catch (ReflectiveOperationException e) {
            logger.error("Error importing plan", e);
        }
        logger.debug("Finished plan file import: {}. Imported plan rows: {}", pi.id, count);
    }

    private boolean validate(PlanImport pi, PlanImportHelper pih) throws InvalidFormatException, IOException {
        logger.debug("Start validating plan file: {}", pi.id);
        logger.debug("Plan file exists: {}", Files.exists(pi.path));
        int colCnt = pih.getCols().size();
        boolean valid = true;
        Path log = Files.createTempFile("planImportComments", null);
        pi.setLog(log);
        Files.copy(pi.path, log, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Plan file copied to: {}", log);
        int count = 0;
        try (InputStream is = Files.newInputStream(log); Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int rowNums = sheet.getPhysicalNumberOfRows();
            if (rowNums < 3)
                return false;
            pih.initWorkbookValidation(wb);
            for (Row row : sheet) {
                if (row.getRowNum() < 2)
                    continue;
                Map<String, PlanField> fields = new HashMap<>(colCnt);
                for (int i = 0; i < colCnt; i++) {
                    PlanCol col = pih.getCols().get(i);
                    Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
                    fields.put(col.getColName(), new PlanField(col, cell, i));
                }
                valid = validateRow(pih, row, fields) && valid;
                count++;
            }
            if (!valid) {
                try (OutputStream os = Files.newOutputStream(log)) {
                    wb.write(os);
                }
            }
        }
        logger.debug("Finish validating plan file: {}. Validated row count: {}. Validation result: {}", pi.id, count,
                valid);
        return valid;
    }

    private boolean validateRow(PlanImportHelper pih, Row row, Map<String, PlanField> fields) {
        boolean valid = true;
        for (Map.Entry<String, PlanField> entry : fields.entrySet()) {
            PlanField field = entry.getValue();
            PlanCol col = field.col;
            Cell cell = field.cell;
            if (col.getRequired() && cell == null) {
                valid = false;
                cell = row.createCell(field.index);
                pih.addComment(row, cell, "REQUIRED_FIELD");
            } else if (cell != null) {
                Object val;
                switch (col.getColType()) {
                    case "text":
                        val = pih.getCellTextVal(cell);
                        valid = validateTextField(pih, row, field, val, fields) && valid;
                        break;
                    case "numeric":
                    case "integer":
                    case "bigint":
                        val = pih.getCellVal(cell);
                        valid = validateNumField(pih, row, cell, col, val, fields) && valid;
                        break;
                    default:
                }
            }
        }
        return valid;
    }

    private boolean validateTextField(PlanImportHelper pih, Row row, PlanField field, Object val,
                                      Map<String, PlanField> fields) {
        PlanCol col = field.col;
        Cell cell = field.cell;
        if (col.getRequired() && (val == null || val.toString().trim().isEmpty())) {
            pih.addComment(row, cell, "REQUIRED_FIELD");
            return false;
        }
        String text = val.toString().trim();
        String name = col.getColName();
        String errCode = null;
        if (text.length() > 2000) {
            pih.addComment(row, cell, "VALUE_TOO_LONG");
            return false;
        }
        switch (name) {
            case "plan_number":
                int fyear = pih.getFinYear(fields);
                if (fyear > 0 && !pih.uniquePlanNumber(text, fyear)) {
                    errCode = "PLAN_NUMBER_EXISTS";
                }
                break;
            case "item_code":
                if (!pih.validItemCode(text)) {
                    errCode = "NOT_IN_LIST";
                }
                break;
            case "purchase_method":
                if (!pih.validPurchMethod(text)) {
                    errCode = "WRONG_PURCHASE_METHOD";
                }
                break;
            case "uom_code":
                if (!pih.validUomCode(text)) {
                    errCode = "NOT_IN_LIST";
                }
                break;
            case "purchase_type":
                if (!pih.validPurchType(text)) {
                    errCode = "WRONG_PURCHASE_TYPE";
                }
                break;
            case "incoterms2010":
                if (!pih.validIncoterms2010(text)) {
                    errCode = "NOT_IN_LIST";
                }
                break;
            case "shipping_region":
                if (!pih.validRegion(text)) {
                    errCode = "NOT_IN_LIST";
                }
                break;
            case "purchase_priority":
                if (!pih.validPurchasePriority(text)) {
                    errCode = "NOT_IN_LIST";
                }
                break;
            default:
                return true;
        }
        if (errCode != null) {
            pih.addComment(row, cell, errCode);
            return false;
        }
        return true;
    }

    private boolean validateNumField(PlanImportHelper pih, Row row, Cell cell, PlanCol col, Object val,
                                     Map<String, PlanField> fields) {
        if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
            pih.addComment(row, cell, "NUMBER_EXPECTED");
            return false;
        }
        String name = col.getColName();
        String errCode = null;
        switch (name) {
            case "financial_year":
                int year = ((Double) val).intValue();
                if (year < pih.getCurrYear()) {
                    errCode = "FINANCIAL_YEAR_NOT_PAST";
                }
                break;
            case "kz_content":
                int perc = ((Double) val).intValue();
                if (perc < 0 || perc > 100) {
                    errCode = "PERCENT_WRONG_RANGE";
                }
                break;
            case "quantity":
            case "unit_price":
            case "amount_without_vat":
                Double v = (Double) val;
                if (pih.isGood(fields) && v < 0) {
                    errCode = "NUMBER_SHOULD_POSITIVE";
                }
                break;
            default:
                return true;
        }
        if (errCode != null) {
            pih.addComment(row, cell, errCode);
            return false;
        }
        return true;
    }

}
