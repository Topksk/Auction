package com.bas.auction.plans;

import com.bas.auction.plans.dto.PlanCol;
import com.bas.auction.profile.customer.setting.dao.PlanColDAO;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component
public class PlanTemplateGenerator {
	private final static Logger logger = LoggerFactory.getLogger(PlanTemplateGenerator.class);

	static {
		System.setProperty("javax.xml.parsers.SAXParserFactory",
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}

	private final PlanColDAO planColDAO;

	@Autowired
	public PlanTemplateGenerator(PlanColDAO planColDAO) {
		this.planColDAO = planColDAO;
	}

	public void generateTemplate(long settingId, OutputStream os) throws IOException {
		logger.debug("generate plan template: {}", settingId);
		List<PlanCol> cols = planColDAO.findSettingPlanColList(settingId);
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Plans");
			sheet.createFreezePane(0, 2);
			POIXMLProperties xmlProps = wb.getProperties();
			POIXMLProperties.CoreProperties cp = xmlProps.getCoreProperties();
			cp.setCreator("BAS");
			cp.setDescription("Template file for importing plans");
			cp.setTitle("Plans");
			CellStyle style = wb.createCellStyle();
			style.setBorderBottom(CellStyle.BORDER_THIN);
			style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
			style.setBorderLeft(CellStyle.BORDER_THIN);
			style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
			style.setBorderRight(CellStyle.BORDER_THIN);
			style.setRightBorderColor(IndexedColors.BLACK.getIndex());
			style.setBorderTop(CellStyle.BORDER_THIN);
			style.setTopBorderColor(IndexedColors.BLACK.getIndex());
			style.setAlignment(CellStyle.ALIGN_CENTER);
			style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			style.setWrapText(true);
			// Create a row and put some cells in it. Rows are 0 based.
			Row row = sheet.createRow(0);
			int k = 0;
			for (PlanCol col : cols) {
				if (!col.getDisplayInTemplate())
					continue;
				// Create a cell and put a value in it.
				Cell cell = row.createCell(k);
				cell.setCellValue(col.getDescription());
				// Style the cell with borders all around.
				cell.setCellStyle(style);
				k++;
			}
			row = sheet.createRow(1);
			for (int i = 0; i < k; i++) {
				// Create a cell and put a value in it.
				Cell cell = row.createCell(i);
				cell.setCellValue(i + 1);
				// Style the cell with borders all around.
				cell.setCellStyle(style);
			}
			wb.write(os);
		}
	}
}
