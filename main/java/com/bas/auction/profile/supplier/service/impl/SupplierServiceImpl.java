package com.bas.auction.profile.supplier.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.dto.UserCertInfo;
import com.bas.auction.billing.service.BillPlanService;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.MessageDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.core.utils.MailService;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.profile.address.service.AddressService;
import com.bas.auction.profile.bankaccount.service.BankAccountService;
import com.bas.auction.profile.employee.dto.Employee;
import com.bas.auction.profile.employee.service.EmployeeService;
import com.bas.auction.profile.supplier.dao.SupplierDAO;
import com.bas.auction.profile.supplier.dto.Supplier;
import com.bas.auction.profile.supplier.service.OrgIdentificationNumberExistsException;
import com.bas.auction.profile.supplier.service.SupplierNotificationService;
import com.bas.auction.profile.supplier.service.SupplierService;
import com.bas.auction.search.SearchService;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplierServiceImpl implements SupplierService {
    private final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);
    private final SupplierDAO supplierDAO;
    private final DocFileDAO docFileDAO;
    private final DocFileAttributeDAO docFileAttributeDAO;
    private final MessageDAO messages;
    private final Conf conf;
    private final SupplierNotificationService supplierNotificationService;
    private final BillPlanService billPlanService;
    private EmployeeService employeeService;
    private AddressService addressService;
    private BankAccountService bankAccountService;

    @Autowired
    public SupplierServiceImpl(DaoJdbcUtil daoutil, SearchService searchService, Conf conf, DocFileDAO docFileDAO,
                               DocFileAttributeDAO docFileAttributeDAO, MessageDAO messages, MailService mailService,
                               SupplierNotificationService supplierNotificationService, SupplierDAO supplierDAO, BillPlanService billPlanService) {
        this.docFileAttributeDAO = docFileAttributeDAO;
        this.supplierDAO = supplierDAO;
        this.conf = conf;
        this.docFileDAO = docFileDAO;
        this.messages = messages;
        this.supplierNotificationService = supplierNotificationService;
        this.billPlanService = billPlanService;
    }

    @Autowired
    public void setEmployeeDAO(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Autowired
    public void setAddressDAO(AddressService addressDAO) {
        this.addressService = addressDAO;
    }

    @Autowired
    public void setBankAccountDAO(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @Override
    @SpringTransactional
    public void register(UserCertInfo certInfo, Employee emp, Supplier supp) throws IOException {
        if (certInfo.isNonResident())
            logger.debug("register non resident supplier");
        else
            logger.debug("register resident supplier");
        if (certInfo.isSupplierExists()) {
            throw new OrgIdentificationNumberExistsException();
        }
        supp.setNonresident(certInfo.isNonResident());
        supp.setLegalEntity(certInfo.isLegalEntity());
        supp.setCountry(certInfo.getCountry());
        supp.setIdentificationNumber(certInfo.getBin());
        emp.setMainUser(true);
        emp.setActive(true);
        if (certInfo.isNonResident()) {
            supp.setRnn(null);
        }
        validateSupplierRegData(supp);
        supp = create(supp);
        long id = supp.getSupplierId();
        emp = employeeService.registerSupplierMainUser(certInfo, emp);
        insertUserAgreement(id);
        billPlanService.createSupplierBillPlan("base", null, null, employeeService.getEmployeeUser(emp));
    }

    private Supplier create(Supplier supplier) {
        supplier.setRegStatus("IN_PROGRESS");
        if (supplier.isNonresident())
            supplier.setBusinessEntityType("CUSTOM");
        else
            supplier.setBusinessEntityTypeCustom(null);
        return supplierDAO.insert(User.sysadmin(), supplier);
    }

    private void validateSupplierRegData(Supplier supp) {
        logger.debug("validate supplier reg data");
        List<String> res = new ArrayList<>();
        if (!supp.isNonresident()) {
            if (!Validator.isValidIinOrBin(supp.getIdentificationNumber()))
                res.add("INVALID_IIN_BIN");
            if (supp.getRnn() != null && !Validator.isValidRnn(supp.getRnn()))
                res.add("INVALID_RNN");
        }
        if (!res.isEmpty())
            throw new ApplException(res);
    }

    private void insertUserAgreement(long supplierId) throws IOException {
        logger.debug("insert user agreement for supplier: {}", supplierId);
        Path file = Paths.get(conf.getUserAgreementFilePath());
        Map<String, String> attributes = new HashMap<>();
        attributes.put("supplier_id", String.valueOf(supplierId));
        attributes.put("file_type", "USER_AGREEMENT");
        String name = messages.get("AGREEMENT_FILE");
        docFileDAO.create(name, 1L, Boolean.TRUE, file, attributes);
    }

    @Override
    @SpringTransactional
    public Supplier sendForApproval(User user, Long supplierId) {
        List<String> msgs = new ArrayList<>();
        if (!bankAccountService.supplierMainBankAccountExists(supplierId))
            msgs.add("MAIN_BANK_ACC_REQUIRED");
        if (!addressService.supplierLegalAddressExists(supplierId))
            msgs.add("LEGAL_ADDRESS_REQUIRED");
        if (!msgs.isEmpty())
            throw new ApplException(msgs);
        docFileAttributeDAO.create(user.getUserId(), "supplier_id", String.valueOf(supplierId), "read_only", "Y");
        return supplierDAO.updateRegStatus(user, supplierId, "SENT_FOR_APPROVAL");
    }

    @Override
    @SpringTransactional
    public Supplier confirmRegistration(User user, Long supplierId) {
        List<String> msgs = new ArrayList<>();
        if (!bankAccountService.supplierMainBankAccountExists(supplierId))
            msgs.add("MAIN_BANK_ACC_REQUIRED");
        if (!addressService.supplierLegalAddressExists(supplierId))
            msgs.add("LEGAL_ADDRESS_REQUIRED");
        if (!msgs.isEmpty())
            throw new ApplException(msgs);
        docFileAttributeDAO.create(user.getUserId(), "supplier_id", String.valueOf(supplierId), "read_only", "Y");
        return supplierDAO.updateRegStatus(user, supplierId, "APPROVED");
    }

    @Override
    @SpringTransactional
    public Supplier approve(User user, Long supplierId) {
        logger.debug("approve supplier registration: {}", supplierId);
        List<String> msgs = new ArrayList<>();
        if (!bankAccountService.supplierMainBankAccountExists(supplierId))
            msgs.add("MAIN_BANK_ACC_REQUIRED");
        if (!addressService.supplierLegalAddressExists(supplierId))
            msgs.add("LEGAL_ADDRESS_REQUIRED");
        if (!msgs.isEmpty())
            throw new ApplException(msgs);
        supplierDAO.updateRegStatus(user, supplierId, "APPROVED");
        supplierNotificationService.sendRegStatusMail(supplierId, true, null);
        return supplierDAO.findById(user, supplierId);
    }

    @Override
    @SpringTransactional
    public Supplier reject(User user, Long supplierId) {
        logger.debug("reject supplier registration: {}", supplierId);
        docFileAttributeDAO.delete("supplier_id", String.valueOf(supplierId), "read_only", "Y");
        supplierDAO.updateRegStatus(user, supplierId, "REJECTED");
        supplierNotificationService.sendRegStatusMail(supplierId, false, "Reject");
        return supplierDAO.findById(user, supplierId);
    }

    @Override
    public List<Long> findEmptyNotificationSetting() {
        return supplierDAO.findEmptyNotificationSetting();
    }

    @SpringTransactional
    public void getFBOrderInfo(String sqlCode, Object[] values) throws ServletException, IOException{
        try {
            URL url = new URL("http://epay.fortebank.com:8443/Exec");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Host", "epay.fortebank.com");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestMethod("POST");
            OutputStream outputStream = conn.getOutputStream();
            String xmlOut = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                    "<TKKPG>\n" +
                    "<Request>\n" +
                    "<Operation>GetOrderInformation</Operation>\n" +
                    "<Language>RU</Language>\n" +
                    "<Order>\n" +
                    "<Merchant>TOPKSK02002129</Merchant>\n" +
                    "<OrderID>"+ values[1].toString() +"</OrderID>\n" +
                    "</Order>\n" +
                    "<SessionID>"+ values[4].toString() +"</SessionID>\n" +
                    "<ShowParams>true</ShowParams>\n" +
                    "<ShowOperations>true</ShowOperations>\n" +
                    "<ClassicView>true</ClassicView>\n" +
                    "</Request>\n" +
                    "</TKKPG>";

            StringReader xmlRead = new StringReader(xmlOut);
            IOUtils.copy(xmlRead, outputStream);
            outputStream.flush();
            String response1 = IOUtils.toString(conn.getInputStream(), "UTF-8");
            String num = null;
            String owner = null;
            if (!values[3].equals("CANCELED")) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(response1)));
                NodeList nodesMain = doc.getElementsByTagName("OrderParams");
                Element orderMain = (Element) nodesMain.item(0);
                NodeList nodes = orderMain.getElementsByTagName("row");
                Integer rowLen = orderMain.getElementsByTagName("row").getLength();
                for (int i = 0; i < rowLen; i++) {
                    Element cardNum = (Element) nodes.item(i);
                    String numCheck = cardNum.getElementsByTagName("PARAMNAME").item(0).getTextContent();
                    if (numCheck.equals("PAN")) {
                num = cardNum.getElementsByTagName("VAL").item(0).getTextContent();
                    }

                    Element cardOwner = (Element) nodes.item(i);
                    String ownerCheck = cardOwner.getElementsByTagName("PARAMNAME").item(0).getTextContent();
                    if (ownerCheck.equals("CardHolderName")) {
                owner = cardOwner.getElementsByTagName("VAL").item(0).getTextContent();
            }
                }

            }
            Object[] newParams;
            newParams = new Object[7];
            int valLen = values.length;
            for (int i = 0; i < values.length; i++) {
                newParams[i] = values[i];
            }
            newParams[valLen] = num;
            newParams[valLen+1] = owner;
            supplierDAO.insertFBStatus(sqlCode, newParams);

        }
        catch (Exception e) {
            logger.info(e.getMessage());
            logger.info("getFBOrderInfo = "+e);
        }
    }
}
