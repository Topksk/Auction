package com.bas.auction.core.impl;

import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class ConfImpl implements Conf {
    private static final Logger logger = LoggerFactory.getLogger(ConfImpl.class);
    private static final long ONE_MINUTE = 60 * 1000;
    private static volatile Properties prop;
    private static volatile long lastRead = 0;
    private static volatile long lastModified = -1;

    private Properties getInstance() {
        long timeElapsed = System.currentTimeMillis() - lastRead;
        if (prop != null && timeElapsed < ONE_MINUTE)
            return prop;
        String path = System.getProperty("etp.conf.file");
        logger.info("Read conf from 1 {" + path + "}");
        if (path == null || path.isEmpty()) {
            path = System.getenv("ETP_CONF_FILE");
            logger.info("1.path ETP_CONF_FILE={" + path + "}");
            //path = path.replace("\n","");
            path=path.replaceAll("\\p{Cntrl}", "");
            logger.info("2. path ETP_CONF_FILE={" + path + "}");
            //path=path.replaceAll("[\\x00-\\x1F]", "");
            //logger.info("3. path ETP_CONF_FILE={" + path + "}");
            logger.info("path.length="+path.length());

            if (path == null || path.isEmpty()) {
                logger.error("Neither etp.conf.file property nor ETP_CONF_FILE environment variable are set");
                return null;
            }
        }
        Path file = Paths.get(path);
        if (prop != null && lastModified == file.toFile().lastModified()) {
            lastRead = System.currentTimeMillis();
            return prop;
        }
        logger.info("Read conf from {" + path + "}");
        synchronized (ConfImpl.class) {
            Properties tmp = null;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                tmp = new Properties();
                tmp.load(reader);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            lastModified = file.toFile().lastModified();
            lastRead = System.currentTimeMillis();
            prop = tmp;
            return tmp;
        }
    }

    @Override
    public String get(String key) {
        return getInstance().getProperty(key);
    }

    @Override
    public String getFunctionalCurrency() {
        return get("currency");
    }

    @Override
    public String getHost() {
        return get("host");
    }

    @Override
    public String getInstructionsUrl() {
        return get("instructions.url");
    }

    @Override
    public String getRootHost() {
        return get("root.host");
    }

    @Override
    public String getCACertsPath() {
        return get("cacerts.path") + File.separator;
    }

    @Override
    public String getNonresidentOID() {
        return get("nonresident.oid");
    }

    @Override
    public String getCrlPath() {
        return get("crl.path") + File.separator;
    }

    @Override
    public String getReportTemplatesPath() {
        return get("report.templates") + File.separator;
    }

    @Override
    public String getFontPath() {
        return get("font.path") + File.separator;
    }

    @Override
    public String getLoginHost() {
        try {
            URL loginUrl = new URL(getLoginUrl());
            String loginUrlProtocol = loginUrl.getProtocol();
            return loginUrlProtocol + "://" + loginUrl.getAuthority();
        } catch (Exception e) {
            logger.error("Error while evaluating login host", e);
            throw new ApplException(e.getMessage());
        }
    }

    @Override
    public String getLoginUrl() {
        return get("login.url");
    }

    @Override
    public String getCspReportPath() {
        return get("cspreport.path") + File.separator;
    }

    @Override
    public String getElasticsearchHost() {
        return get("elasticsearch.host");
    }

    @Override
    public String getElasticsearchPort() {
        return get("elasticsearch.port");
    }

    @Override
    public String getFileStorePath() {
        return get("file.path") + File.separator;
    }

    @Override
    public String getPublicFileStorePath() {
        return get("public.file.path") + File.separator;
    }

    @Override
    public String getUserCertsStorePath() {
        return get("user.certs.path") + File.separator;
    }

    @Override
    public String getResidentCountry() {
        return get("resident.country");
    }

    @Override
    public String getUserAgreementFilePath() {
        return get("agreement");
    }

    @Override
    public String getCurrencyExchangeRateLoadUrl() {
        return get("exchange.rate.url");
    }

    @Override
    public int getSendBidPricesThreadpoolMaxSize() {
        String size = get("send_bid_prices.threadpool.maxsize");
        int s = 20;
        if (StringUtils.isNumeric(size))
            s = Integer.parseInt(size);
        return s;
    }

    @Override
    public String getDbUrl() {
        return get("db.url");
    }

    @Override
    public String getDbUser() {
        return get("db.user");
    }

    @Override
    public String getDbPassword() {
        return get("db.pass");
    }

    @Override
    public String getElasticsearchClusterName() {
        return get("elasticsearch.cluster");
    }

    @Override
    public int getPlanImportThreadPoolMaxSize() {
        String size = get("plan_import.threadpool.maxsize");
        int s = 20;
        if (StringUtils.isNumeric(size))
            s = Integer.parseInt(size);
        return s;
    }

    @Override
    public int getMailThreadPoolMaxSize() {
        String size = get("mail.threadpool.maxsize");
        int s = 20;
        if (StringUtils.isNumeric(size))
            s = Integer.parseInt(size);
        return s;
    }

    @Override
    public String getSmtpHost() {
        return get("mail.smtp.host");
    }

    @Override
    public String getSmtpAuth() {
        return get("mail.smtp.auth");
    }

    @Override
    public String getSmtpPort() {
        return get("mail.smtp.port");
    }

    @Override
    public String getSmtpStarttlsEnable() {
        return get("mail.smtp.starttls.enable");
    }

    @Override
    public String getMailContentType() {
        return get("mail.content.type");
    }

    @Override
    public String getMailSendFrom() {
        return get("mail.send.from");
    }

    @Override
    public String getMailSenderPassword() {
        return get("mail.sender.pass");
    }

    @Override
    public boolean isFunctionalCurrency(String bidCurrency) {
        return getFunctionalCurrency().equals(bidCurrency);
    }

    @Override
    public String getExternalSiteURL() {
        return get("external.site.url");
    }

    @Override
    public String getExternalSiteRoot() {
        return get("external.site.root");
    }

    @Override
    public boolean isNegPublishPeriodValidationEnabled() {
        return Boolean.valueOf(get("neg.publish.period.validation.enabled"));
    }
}
