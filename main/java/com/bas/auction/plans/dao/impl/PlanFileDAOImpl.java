package com.bas.auction.plans.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.ApplException;
import com.bas.auction.core.Conf;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.plans.dao.PlanFileDAO;
import com.bas.auction.plans.dao.PlanImportWorker;
import com.bas.auction.plans.dto.PlanFile;
import com.bas.auction.plans.dto.PlanImport;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Repository
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanFileDAOImpl implements PlanFileDAO, GenericDAO<PlanFile> {
    private final static Logger logger = LoggerFactory.getLogger(PlanFileDAOImpl.class);
    private final DaoJdbcUtil daoutil;
    private final TransactionTemplate transactionTemplate;
    private final DocFileDAO docFileDAO;
    private final Conf conf;
    private final PlanImportWorker planImportWorker;

    @Autowired
    public PlanFileDAOImpl(DaoJdbcUtil daoutil, PlatformTransactionManager transactionManager, Conf conf,
                           DocFileDAO docFileDAO, PlanImportWorker planImportDAO) {
        this.daoutil = daoutil;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.conf = conf;
        this.docFileDAO = docFileDAO;
        this.planImportWorker = planImportDAO;
    }

    @Override
    public String getSqlPath() {
        return "plan_file";
    }

    @Override
    public Class<PlanFile> getEntityType() {
        return PlanFile.class;
    }

    @Override
    public PlanFile findById(Long id) {
        return daoutil.queryForObject(this, "get_plan_file", id);
    }

    @Override
    public List<PlanFile> findCustomerList(Long customerId) {
        logger.debug("list plan files");
        return daoutil.query(this, "plan_file_list", customerId);
    }

    private Integer create(User user, Long fileId) throws IOException {
        Object[] values = {fileId, user.getCustomerId(), user.getUserId(), user.getUserId()};
        KeyHolder kh = daoutil.insert(this, values);
        return (Integer) kh.getKeys().get("plan_file_id");
    }

    private void validateFileType(Path path, String name) throws IOException {
        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = config.getDetector();
        String type;
        try (TikaInputStream stream = TikaInputStream.get(path)) {
            Metadata metadata = new Metadata();
            metadata.add(Metadata.RESOURCE_NAME_KEY, name);
            MediaType mediaType = detector.detect(stream, metadata);
            type = mediaType.getType() + '/' + mediaType.getSubtype();
        }
        logger.debug("Importing file type {}", type);
        if (!XlsMimeType.equals(type) && !XlsxMimeType.equals(type)) {
            Files.deleteIfExists(path);
            throw new ApplException("ILLEGAL_PLAN_FILE_TYPE");
        }
    }

    private Path saveUploadedFile(Part filePart) throws IOException {
        Path tmp = Files.createTempFile("planimport", null);
        try (InputStream bis = new BufferedInputStream(filePart.getInputStream());
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(tmp))) {
            int count;
            byte data[] = new byte[8192];
            while ((count = bis.read(data, 0, 8192)) != -1) {
                os.write(data, 0, count);
            }
        }
        return tmp;
    }

    @Override
    public void create(User user, Part filePart) throws IOException {
        Path tmp = saveUploadedFile(filePart);
        String fileName = filePart.getSubmittedFileName();
        //validateFileType(tmp, fileName);   //Для проверки типа файла
        // Need to commit immediately because plan import executor can process file in separate
        // earlier than this request finishes
        PlanImport planImport = transactionTemplate.execute(status -> createPlanImport(status, tmp, fileName, user));
        planImportWorker.submit(planImport);
        logger.debug("Submitted plan file for import: {}", planImport.id);
        Files.deleteIfExists(tmp);
    }

    private PlanImport createPlanImport(TransactionStatus status, Path tmp, String fileName, User user) {
        try {
            Long fileId = docFileDAO.create(fileName, user.getUserId(), false, tmp, null);
            Integer id = create(user, fileId);
            String path = conf.getFileStorePath() + fileId;
            return new PlanImport(id, Paths.get(path), user, fileId);
        } catch (Exception e) {
            logger.error("Error creating plan file", e);
            status.setRollbackOnly();
        }
        return null;
    }

    @Override
    @SpringTransactional
    public void delete(User user, Long id) throws IOException {
        logger.debug("delete plan file: {}", id);
        PlanFile pf = findById(id);
        if (pf == null) {
            logger.warn("plan file not found: {}", id);
            return;
        } else if (pf.getCustomerId() != user.getCustomerId()) {
            logger.warn("permission denied for removing plan file: {}", id);
            return;
        }
        daoutil.delete(this, new Object[]{id});
        docFileDAO.delete(user, pf.getFileId());
        docFileDAO.delete(user, pf.getParseLogFileId());
    }
}
