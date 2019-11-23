package com.bas.auction.currency.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.Conf;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.currency.dao.CurrencyDAO;
import com.bas.auction.currency.dto.Currency;
import com.bas.auction.currency.dto.ExchangeRate;
import com.bas.auction.currency.service.ExchangeRateLoadException;
import com.bas.auction.currency.service.ExchangeRateLoaderService;
import com.bas.auction.currency.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class ExchangeRateLoaderServiceImpl implements ExchangeRateLoaderService {
    private final static Logger logger = LoggerFactory.getLogger(ExchangeRateLoaderServiceImpl.class);
    private final CurrencyDAO currencyDAO;
    private final ExchangeRateService rateService;
    private final Conf conf;
    private final XPathExpression itemsExp;
    private final XPathExpression currExp;
    private final XPathExpression rateExp;
    private final XPathExpression quantExp;

    @Autowired
    public ExchangeRateLoaderServiceImpl(Conf conf, CurrencyDAO currencyDAO, ExchangeRateService rateDAO) throws Exception {
        this.conf = conf;
        this.currencyDAO = currencyDAO;
        this.rateService = rateDAO;

        XPath xPath = XPathFactory.newInstance().newXPath();
        this.itemsExp = xPath.compile("/rates/item");
        this.currExp = xPath.compile("title");
        this.rateExp = xPath.compile("description");
        this.quantExp = xPath.compile("quant");
    }

    @Override
    @SpringTransactional
    public synchronized void fetchAndSaveRates() {
        Set<String> currencies = currencyDAO.findAll().stream()
                .filter(c -> c.isActive() && !c.isFunctionalCurrency())
                .map(Currency::getCode)
                .collect(toSet());
        List<ExchangeRate> rates = getDates().stream()
                .map(date -> fetchRates(date, currencies))
                .flatMap(List::stream)
                .collect(toList());
        if (!rates.isEmpty())
            rateService.create(User.sysadmin(), rates);
    }

    private List<Date> getDates() {
        List<Date> dates = new ArrayList<>();
        Calendar cal = getCalendar();
        long days = getIntervalDays(cal);
        for (int i = 0; i < days; i++) {
            cal.add(Calendar.DATE, 1);
            dates.add(new Date(cal.getTime().getTime()));
        }
        return dates;
    }

    private Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();
        Date maxDate = rateService.findRateAvailableMaxDate();
        cal.setTime(maxDate);
        return cal;
    }

    private long getIntervalDays(Calendar cal) {
        Date today = new Date();
        long interval = today.getTime() - cal.getTime().getTime();
        return TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS);
    }

    private List<ExchangeRate> fetchRates(Date date, Set<String> currencies) {
        logger.debug("start fetching exchange rates: currencies={}", currencies);
        try {
            HttpURLConnection conn = openConnection(date);
            try (Reader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                InputSource is = new InputSource(reader);
                NodeList nl = (NodeList) itemsExp.evaluate(is, XPathConstants.NODESET);
                int length = nl.getLength();
                List<ExchangeRate> res = new ArrayList<>();
                for (int i = 0; i < length; i++) {
                    Node item = nl.item(i);
                    String currency = currExp.evaluate(item);
                    if (!currencies.contains(currency))
                        continue;
                    String rate = rateExp.evaluate(item);
                    String quant = quantExp.evaluate(item);
                    logger.debug("currency: {}, rate: {}, quant: {}", currency, rate, quant);
                    ExchangeRate er = createExchangeRate(currency, date, rate, quant);
                    res.add(er);
                    if (res.size() == currencies.size())
                        break;
                }
                logger.debug("exchange rates are fetched");
                return res;
            }
        } catch (Exception e) {
            logger.error("error loading exchange rate: date={}", date, e);
            throw new ExchangeRateLoadException();
        }
    }

    private HttpURLConnection openConnection(Date date) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String fdate = sdf.format(date);
        logger.debug("opening connection: date={}", fdate);
        String address = conf.getCurrencyExchangeRateLoadUrl() + fdate;
        URL url = new URL(address);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);
        int serverResponseCode = conn.getResponseCode();
        if (serverResponseCode != HttpURLConnection.HTTP_OK) {
            String serverErr = "HTTP response code " + serverResponseCode + ". " + conn.getResponseMessage();
            throw new ExchangeRateLoadException(serverErr);
        }
        return conn;
    }

    private ExchangeRate createExchangeRate(String currency, Date date, String rate, String quant) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromCurrency("KZT");
        exchangeRate.setToCurrency(currency);
        exchangeRate.setRate(new BigDecimal(rate));
        exchangeRate.setActiveDate(date);
        exchangeRate.setQuant(Integer.parseInt(quant));
        return exchangeRate;
    }
}
