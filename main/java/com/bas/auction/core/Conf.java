package com.bas.auction.core;

public interface Conf {

	String get(String key);

	String getFunctionalCurrency();

	String getHost();

	String getInstructionsUrl();

	String getRootHost();

	String getCACertsPath();

	String getNonresidentOID();

	String getCrlPath();

	String getReportTemplatesPath();

	String getFontPath();

	String getLoginHost();

	String getLoginUrl();

	String getCspReportPath();

	String getElasticsearchHost();

	String getElasticsearchPort();

	String getFileStorePath();

	String getPublicFileStorePath();

	String getUserCertsStorePath();

	String getResidentCountry();

	String getUserAgreementFilePath();

	String getCurrencyExchangeRateLoadUrl();

	int getSendBidPricesThreadpoolMaxSize();

	String getDbUrl();

	String getDbUser();

	String getDbPassword();

	String getElasticsearchClusterName();

	int getPlanImportThreadPoolMaxSize();

	int getMailThreadPoolMaxSize();

	String getSmtpHost();

	String getSmtpAuth();

	String getSmtpPort();

	String getSmtpStarttlsEnable();

	String getMailContentType();

    String getMailSendFrom();

    String getMailSenderPassword();

	boolean isFunctionalCurrency(String bidCurrency);

	String getExternalSiteURL();

	String getExternalSiteRoot();

	boolean isNegPublishPeriodValidationEnabled();
}
