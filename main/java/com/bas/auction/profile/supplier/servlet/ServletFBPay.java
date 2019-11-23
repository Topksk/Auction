package com.bas.auction.profile.supplier.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import java.net.URL;
import java.net.HttpURLConnection;
import java.sql.*;


/**
 * Created by sauran.alteyev on 25.04.2017.
 */
@WebServlet(name = "ServletFBPay")
public class ServletFBPay extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(ServletFBPay.class);
    private static final String CONTENT_TYPE = "Content-Type";
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        try {
            Object[] values = null;
            response.setContentType(CONTENT_TYPE);
            URL url = new URL("http://epay.fortebank.com:8443/Exec");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Host", "epay.fortebank.com");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestMethod("POST");
            OutputStream outputStream = conn.getOutputStream();
            String amount = request.getParameter("fillSum");
            String outAmount = amount+"00";
            String compId = request.getParameter("compId");
            String xmlOut = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<TKKPG>\n" +
                            "<Request>" +
                            "<Operation>CreateOrder</Operation>\n" +
                            "<Language>RU</Language>\n" +
                            "<Order>\n" +
                            "<OrderType>Purchase</OrderType>\n" +
                            "<Merchant>TOPKSK02002129</Merchant>\n" +
                            "<Amount>"+outAmount+"</Amount>\n" +
                            "<Currency>398</Currency>\n" +
                            "<Description>Пополнение баланса Специалиста</Description>\n" +
                            "<ApproveURL>https://topksk.kz/approvepay.html</ApproveURL>\n" +
                            "<CancelURL>https://topksk.kz/servrequest.html</CancelURL>\n" +
                            "<DeclineURL>https://topksk.kz/declinepay.html</DeclineURL>\n" +
                            "<AddParams>\n" +
                            "<OrderExpirationPeriod>30</OrderExpirationPeriod>\n" +
                            "</AddParams>\n" +
                            "<Fee></Fee>\n" +
                            "</Order>\n" +
                            "</Request>\n" +
                            "</TKKPG>";

            StringReader xmlRead = new StringReader(xmlOut);
            IOUtils.copy(xmlRead, outputStream, "windows-1251");
            outputStream.flush();
            String response1 = IOUtils.toString(conn.getInputStream(), "UTF-8");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(response1)));
            NodeList nodesMain = doc.getElementsByTagName("TKKPG");
            Element orderMain = (Element)nodesMain.item(0);
            String status = orderMain.getElementsByTagName("Status").item(0).getTextContent();
            logger.info("status = "+status);
            if (status.equals("00")) {
                NodeList nodes = doc.getElementsByTagName("Order");
                Element order = (Element)nodes.item(0);
                String orderId = order.getElementsByTagName("OrderID").item(0).getTextContent();
                String sessId = order.getElementsByTagName("SessionID").item(0).getTextContent();
                String url_red = order.getElementsByTagName("URL").item(0).getTextContent();
                String targetUrl = url_red+"?SessionID="+sessId+"&OrderID="+orderId;
                values = new Object[]{
                        compId,
                        orderId,
                        amount,
                        "CREATED",
                        sessId
                };

                    doInsert(values);
                    redirectStrategy.sendRedirect(request, response, targetUrl);
            }



        }
        catch (Exception e) {
            logger.info(e.getMessage());
            logger.info("ServletFBPay = "+e);
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }


    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String password = "postgres";


    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    protected void doInsert(Object[] values) throws SQLException {
        try {
            String INS = "insert into ch_ksk.t_pay (t_company_id,order_id,amount,status,session_id)\n" +
                    "values (cast("+values[0].toString()+" as bigint),\n" +
                            "cast("+values[1].toString()+" as bigint),\n" +
                            "cast("+values[2].toString()+" as numeric(10,2)),\n" +
                            "'"+values[3].toString()+"',\n" +
                            "'"+values[4].toString()+"');";
            Connection conn = this.connect();
            Statement pstmt = conn.createStatement();
            pstmt.executeUpdate(INS);
            logger.info("Insert successfully");
        }
        catch (Exception e) {
            logger.info("doInsert = "+e);
        }
    }
}
