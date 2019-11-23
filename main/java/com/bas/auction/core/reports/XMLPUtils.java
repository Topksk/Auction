package com.bas.auction.core.reports;

import com.bas.auction.core.Conf;
import oracle.apps.xdo.XDOException;
import oracle.apps.xdo.template.FOProcessor;
import oracle.apps.xdo.template.RTFProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Properties;

@Component
public class XMLPUtils {
    private final static Logger logger = LoggerFactory.getLogger(XMLPUtils.class);
    private final Properties prop = new Properties();

    @Autowired
    public XMLPUtils(Conf conf) throws IOException {
        try (InputStream is = XMLPUtils.class
                .getResourceAsStream("/com/bas/auction/core/reports/conf/xdo.properties")) {
            logger.debug("reading xmlp properties");
            prop.load(is);
            prop.put("system-temp-dir", System.getProperty("java.io.tmpdir"));
            String fontDir = conf.getFontPath().replaceAll("%20", "\\ ");
            for (Object key : prop.keySet()) {
                String k = (String) key;
                if (k.startsWith("font.")) {
                    String result = MessageFormat.format((String) prop.get(k), fontDir);
                    prop.setProperty(k, result);
                }
            }
        }
    }

    public void generateXslTempl(InputStream inPath, String outPath) throws IOException, XDOException {
        RTFProcessor rtf = new RTFProcessor(inPath);
        rtf.setOutput(outPath);
        rtf.process();
    }

    public void generateReport(Reader reader, Reader dataReader, OutputStream os) throws XDOException, IOException {
        FOProcessor processor = new FOProcessor();
        processor.setConfig(prop);
        processor.setData(dataReader);
        processor.setTemplate(reader);
        processor.setOutput(os);
        processor.setOutputFormat(FOProcessor.FORMAT_PDF);
        processor.generate();
    }
}