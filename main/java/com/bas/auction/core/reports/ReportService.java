package com.bas.auction.core.reports;

import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import oracle.apps.xdo.XDOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Map;

@Service
public class ReportService {
    private final static Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final String templateDir;
    private final DocFileDAO docFileDAO;
    private final DataSource dataSource;
    private final MessageDAO messages;
    private final XMLPUtils xmlpUtils;

    @Autowired
    public ReportService(DataSource dataSource, Conf conf, XMLPUtils xmlpUtils, MessageDAO messages, DocFileDAO docFileDAO) {
        this.dataSource = dataSource;
        this.xmlpUtils = xmlpUtils;
        this.docFileDAO = docFileDAO;
        this.messages = messages;
        this.templateDir = conf.getReportTemplatesPath();
    }

    public Path getXslTemplate(String template) throws IOException, XDOException, URISyntaxException {
        String xslName = templateDir + File.separator + template + ".xsl";
        logger.debug("get xsl template: {}", xslName);
        Path templatePath = Paths.get(xslName);
        String rtf = template + ".rtf";
        URL resource = ReportService.class.getResource("/com/bas/auction/core/reports/templates/" + rtf);
        Path rtfTemplate = Paths.get(resource.toURI());
        boolean exists = Files.exists(templatePath);
        if (!exists || rtfTemplate.toFile().lastModified() > templatePath.toFile().lastModified()) {
            if (!exists)
                logger.info("xsl templ file not found: {}", xslName);
            else
                logger.info("xsl templ file modified: {}", xslName);
            synchronized (ReportService.class) {
                Path dir = Paths.get(templateDir);
                if (!Files.exists(dir)) {
                    logger.info("create dir for xsl templates: {}", dir);
                    Files.createDirectory(dir);
                }
                if (!Files.exists(templatePath) || rtfTemplate.toFile().lastModified() > templatePath.toFile().lastModified()) {
                    try (InputStream stream = Files.newInputStream(rtfTemplate)) {
                        String tmp = xslName + ".tmp";
                        logger.debug("gen xsl templ from RTF: rtf = {}", rtfTemplate);
                        xmlpUtils.generateXslTempl(stream, tmp);
                        Path tmpFile = Paths.get(tmp);
                        Path xslFile = Paths.get(xslName);
                        Files.move(tmpFile, xslFile, StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE);
                        logger.debug("replaced tmp xsl file with new one: {}", xslFile);
                    }
                }
            }
        }
        exists = Files.exists(templatePath);
        if (!exists) {
            logger.error("xsl templ file not found: {}", xslName);
        }
        return templatePath;
    }

    public long generateReport(long userId, String reportCode, Map<String, String> attributes,
                               Map<String, Object[]> params) throws SQLException, ParserConfigurationException, TransformerException,
            IOException, XDOException, NoSuchAlgorithmException, XPathExpressionException, URISyntaxException {
        String reportFileName = messages.get(reportCode);
        return generateReport(userId, reportFileName, reportCode, attributes, params);
    }

    public long generateReport(long userId, String reportFileName, String reportCode, Map<String, String> attributes,
                               Map<String, Object[]> params) throws SQLException, ParserConfigurationException, TransformerException,
            IOException, XDOException, NoSuchAlgorithmException, XPathExpressionException, URISyntaxException {
        logger.debug("gen rep: {}", reportCode);
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Object[]> param : params.entrySet()) {
                logger.debug("{} = {}", param.getKey(), param.getValue());
            }
        }
        logger.debug("rep attrs: {}", attributes);
        String data;
        Path output = Files.createTempFile(null, ".pdf");
        logger.debug("rep out file: {}", output);
        long id = -1;
        try {
            data = executeQueries(reportCode, params);
            Path template = getXslTemplate(reportCode);
            try (Reader templReader = Files.newBufferedReader(template, StandardCharsets.UTF_8);
                 Reader dataReader = new StringReader(data);
                 OutputStream os = Files.newOutputStream(output)) {
                xmlpUtils.generateReport(templReader, dataReader, os);
            }
            id = docFileDAO.create(reportFileName, userId, true, output, attributes);
        } finally {
            if (output != null) {
                if (Files.deleteIfExists(output)) {
                    logger.debug("deleted rep file: {}", output);
                } else {
                    logger.error("can't delete rep file: {}", output);
                }
            }
        }
        return id;
    }

    public String executeQueries(String reportCode, Map<String, Object[]> params) throws XPathExpressionException,
            IOException, ParserConfigurationException, TransformerException, SQLException {
        logger.debug("exec queries: {}", reportCode);
        String sqlName = "/com/bas/auction/core/reports/conf/" + reportCode + ".xml";
        try (InputStream is = ReportService.class.getResourceAsStream(sqlName)) {
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathExpression = xPath.compile("/sql/sqlStatement");
            InputSource inputSource = new InputSource(is);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory
                    .newInstance("com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", null);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element data = doc.createElement("DATA");
            doc.appendChild(data);
            NodeList queries = (NodeList) xPathExpression.evaluate(inputSource, XPathConstants.NODESET);
            Connection conn = DataSourceUtils.getConnection(dataSource);
            for (int i = 0; i < queries.getLength(); i++) {
                Element elem = (Element) queries.item(i);
                if (!elem.hasAttribute("name"))
                    continue;
                String name = elem.getAttribute("name").toUpperCase();
                Element group = doc.createElement("LIST_" + name);
                data.appendChild(group);
                String query = xPath.evaluate("text()", elem);
                logger.debug("exec query: {}", name);
                execQuery(conn, doc, group, name, query, params.get(name));
            }
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        }
    }

    private void execQuery(Connection conn, Document doc, Element root, String rowName, String query, Object[] params)
            throws SQLException, TransformerException, ParserConfigurationException {
        logger.trace("exec SQL: {}", query);
        logger.trace("exec params: {}", params);
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int colCnt = md.getColumnCount();
            String[] cols = new String[colCnt];
            for (int i = 0; i < colCnt; i++)
                cols[i] = md.getColumnName(i + 1).toUpperCase();
            while (rs.next()) {
                Element row = doc.createElement(rowName);
                root.appendChild(row);
                for (String col : cols)
                    createElem(doc, row, col, rs.getObject(col));
            }
            rs.close();
        }
    }

    private void createElem(Document doc, Element parent, String tag, Object value) {
        if (value != null)
            createElem(doc, parent, tag, String.valueOf(value));
        else
            createElem(doc, parent, tag, null);
    }

    private void createElem(Document doc, Element parent, String tag, String value) {
        Element elem = doc.createElement(tag);
        if (value != null && !value.trim().isEmpty())
            elem.appendChild(doc.createTextNode(value));
        parent.appendChild(elem);
    }

}
