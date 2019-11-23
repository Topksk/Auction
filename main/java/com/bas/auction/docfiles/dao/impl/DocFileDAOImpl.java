package com.bas.auction.docfiles.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.Conf;
import com.bas.auction.core.crypto.CryptoUtils;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFileDAO;
import com.bas.auction.docfiles.dao.DocFilePermissionDAO;
import com.bas.auction.docfiles.dao.DocFileSignatureDAO;
import com.bas.auction.docfiles.dto.DocFile;
import com.bas.auction.docfiles.dto.DocFileSignature;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Repository
public class DocFileDAOImpl implements DocFileDAO, GenericDAO<DocFile> {
    private final static Logger logger = LoggerFactory.getLogger(DocFileDAOImpl.class);

    static {
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.transform.TransformerFactory",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    }

    private final DocFilePermissionDAO docFilePermissionDAO;
    private final DocFileSignatureDAO docFileSignatureDAO;
    private final DocFileAttributeDAO docFileAttributeDAO;
    private final CryptoUtils cryptoUtils;
    private final DaoJdbcUtil daoutil;
    private final Conf conf;

    @Autowired
    public DocFileDAOImpl(DaoJdbcUtil daoutil, Conf conf, CryptoUtils cryptoUtils,
                          DocFilePermissionDAO docFilePermissionDAO, DocFileSignatureDAO docFileSignatureDAO,
                          DocFileAttributeDAO docFileAttributeDAO) {
        this.daoutil = daoutil;
        this.conf = conf;
        this.cryptoUtils = cryptoUtils;
        this.docFilePermissionDAO = docFilePermissionDAO;
        this.docFileSignatureDAO = docFileSignatureDAO;
        this.docFileAttributeDAO = docFileAttributeDAO;
    }

    @Override
    public String getSqlPath() {
        return "file";
    }

    @Override
    public Class<DocFile> getEntityType() {
        return DocFile.class;
    }

    private boolean canRead(boolean[] filePerms, User user) {
        return user.isSysadmin() || (user.isCustomer() && filePerms[0]) || (user.isSupplier() && filePerms[3]);
    }

    private boolean canSign(boolean[] filePerms, User user) {
        return (user.isCustomer() && filePerms[1]) || (user.isSupplier() && filePerms[4]);
    }

    private boolean canDelete(DocFile doc, boolean[] filePerms, User user) {
        return user.isSysadmin() || user.getUserId() == doc.getOwner()
                && ((user.isCustomer() && filePerms[2]) || (user.isSupplier() && filePerms[5]));
    }

    private boolean canSign(DocFile doc, long userId) {
        return doc.isCanSign() && !userSigned(doc, userId);
    }

    private boolean userSigned(DocFile doc, long userId) {
        return doc.getSignatures() != null &&
                doc.getSignatures().stream().anyMatch(s -> s.getUserId() == userId);
    }

    private String detectContentType(Path path, String name) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            String contentType = new Tika().detect(is, name);
            contentType = contentType == null ? "application/octet-stream" : contentType;
            logger.debug("detected mime type: ", contentType);
            return contentType;
        }
    }

    @Override
    public DocFile findById(User user, Long fileId) {
        logger.trace("get file by id: {}", fileId);
        DocFile file = daoutil.queryForObject(this, "get", fileId);
        if (file != null) {
            boolean[] filePerms = docFilePermissionDAO.findFilePermissions(file.getFileId());
            if (!canRead(filePerms, user)) {
                logger.warn("access denied to reading file: {}", file.getFileId());
                return null;
            }
            file.setCanSign(canSign(filePerms, user));
            file.setCanRemove(canDelete(file, filePerms, user));
        } else
            logger.warn("file not found by id: {}", fileId);
        return file;
    }

    @Override
    public List<DocFile> findByAttr(User user, String attribute, String value) {
        logger.debug("get files by attr: {} = {}", attribute, value);
        List<DocFile> files = daoutil.query(this, "get_by_attr", attribute, value);
        Iterator<DocFile> iter = files.iterator();
        while (iter.hasNext()) {
            DocFile file = iter.next();
            boolean[] filePerms = docFilePermissionDAO.findFilePermissions(file.getFileId());
            if (!canRead(filePerms, user)) {
                logger.warn("access denied to reading file: {}", file.getFileId());
                iter.remove();
                continue;
            }
            file.setCanSign(canSign(filePerms, user));
            file.setCanRemove(canDelete(file, filePerms, user));
            List<DocFileSignature> signatures = docFileSignatureDAO.findFileSignatures(file.getFileId());
            file.setSignatures(signatures);
            file.setCanSign(canSign(file, user.getUserId()));
        }
        return files;
    }

    @Override
    public List<DocFile> findByAttrWithoutProtocols(String attribute, String value) {
        logger.debug("get files by attr: {} = {}", attribute, value);
        return daoutil.query(this, "get_by_attr_without_protocols", attribute, value);
    }

    @Override
    public List<DocFile> findByAttr(User user, String attribute, Object value) {
        return findByAttr(user, attribute, String.valueOf(value));
    }

    @Override
    public List<DocFile> findByAttr(String attribute, String value) {
        logger.debug("get files by attr: {} = {}", attribute, value);
        List<DocFile> files = daoutil.query(this, "get_by_attr", attribute, value);
        return files;
    }

    @Override
    public List<Long> findFileIdsByAttribute(String attribute, String value, String attribute2, String value2) {
        return daoutil.queryScalarList(this, "get_fileids_by_attr", attribute, value, attribute2, value2);
    }

    @Override
    public boolean findIsFileExistsByAttribute(String attribute, String value, String attribute2, String value2) {
        return daoutil.exists(this, "get_fileid_by_attr", attribute, value, attribute2, value2);
    }

    @Override
    public boolean findIsUnsignedFileExistsByAttribute(String attribute, String value) {
        return daoutil.exists(this, "unsigned_file_exists", attribute, value);
    }

    @Override
    public List<Map<String, Object>> findForIntegra(long negId) {
        return daoutil.queryForMapList(this, "get_for_integra", negId);
    }

    @Override
    @SpringTransactional
    public Long create(DocFile docFile) {
        Long userId = docFile.getCreatedBy();
        Object[] values = {docFile.getFileName(), docFile.getFileType(), docFile.getFileSize(),
                docFile.getPath(), userId, docFile.getHashValue(), docFile.getIsSystemGenerated(),
                docFile.getCopiedFileId(), userId, userId, docFile.getTableId(), docFile.getTableName()};

        try
        {
            KeyHolder kh = daoutil.insert(this, values);
            return (Long) kh.getKeys().get("file_id");
        }
        catch(Exception e)
        {
            logger.info(e.getMessage());
            //logger.info("error = "+e);
            throw null;
        }
    }

    @Override
    @SpringTransactional
    public Long createNotifFile(DocFile docFile) {
        Long userId = docFile.getCreatedBy();
        Object[] values = {docFile.getFileName(), docFile.getFileType(), docFile.getFileSize(),
                docFile.getPath(), userId, docFile.getHashValue(), docFile.getIsSystemGenerated(),
                docFile.getCopiedFileId(), userId, userId,userId};
        KeyHolder kh = daoutil.insertNotifFile(this, values);
        return (Long) kh.getKeys().get("file_id");
    }

    @Override
    @SpringTransactional
    public Long create(String name, Long userId, Boolean isSystemGenerated, Path file, Map<String, String> attributes)
            throws IOException {
        String hashValue = cryptoUtils.getDigest(file);
        String fileMIMEType = detectContentType(file, name);
        String ftype=new String();
        long size = Files.size(file);
        DocFile docFile = new DocFile();
        docFile.setCreatedBy(userId);
        docFile.setFileName(name);
        docFile.setFileType(fileMIMEType);
        docFile.setFileSize(size);
        docFile.setPath(conf.getFileStorePath());
        docFile.setHashValue(hashValue);
        docFile.setIsSystemGenerated(isSystemGenerated);
        Long fileId = create(docFile);
        logger.debug("File Myme Type {}"+fileMIMEType);


        if (fileMIMEType.equals("image/png")){
            ftype=".png";
        }else if (fileMIMEType.equals("image/jpeg")){
            ftype=".jpg";
        }else if (fileMIMEType.equals("image/gif")){
            ftype=".gif";
        }else if (fileMIMEType.equals("image/ico")){
            ftype=".ico";
        }else if (fileMIMEType.equals("image/otf")){
            ftype=".otf";
        }else if (fileMIMEType.equals("image/eot")){
            ftype=".eot";
        }else if (fileMIMEType.equals("image/svg")){
            ftype=".svg";
        }else if (fileMIMEType.equals("image/tiff")){
            ftype=".tiff";
        }else if (fileMIMEType.equals("image/psd")){
            ftype=".psd";
        }else if (fileMIMEType.equals("image/x-ms-bmp")){
            ftype=".bmp";
        }else if (fileMIMEType.equals("image/bmp")){
            ftype=".bmp";
        }else if (fileMIMEType.equals("image/raw")){
            ftype=".raw";
        }


        if (attributes != null) {
            attributes.entrySet().forEach(attr -> docFileAttributeDAO.create(userId, fileId, attr.getKey(), attr.getValue()));
        }
        Path to = Paths.get(conf.getFileStorePath() + fileId+ftype);
        Files.copy(file, to, StandardCopyOption.COPY_ATTRIBUTES);
        return fileId;
    }

    @Override
    @SpringTransactional
    public boolean delete(User user, Long fileId) throws IOException {
        DocFile doc = findById(user, fileId);
        return delete(false, doc);
    }

    private boolean delete(boolean force, DocFile doc) throws IOException {
        if (doc == null) {
            return false;
        } else if (!force && !doc.isCanRemove()) {
            logger.error("permission denied for removing file: {}", doc.getFile());
            return false;
        }
        Long fileId = doc.getFileId();
        docFileSignatureDAO.deleteFileSignatures(fileId);
        docFileAttributeDAO.deleteFileAttrs(fileId);
        Object[] values = {fileId};
        daoutil.delete(this, values);
        Path path = doc.getFile();
        if (Files.deleteIfExists(path)) {
            logger.debug("deleted file from system: {}", path);
        } else {
            logger.error("can't delete file from system: {}", path);
        }
        return true;
    }

    @Override
    @SpringTransactional
    public void deleteByAttr(User user, String attribute, Object value, boolean forceRemove) throws IOException {
        logger.debug("removing files by attr: {} = {}", attribute, value);
        List<DocFile> list = findByAttr(user, attribute, value);
        for (DocFile doc : list) {
            delete(forceRemove, doc);
        }
    }

    @Override
    @SpringTransactional
    public void deleteByAttr(User user, String attribute, Object value, String attribute2, Object value2)
            throws IOException {
        List<Long> ids = daoutil.queryScalarList(this, "get_fileid_by_attr", attribute, String.valueOf(value),
                attribute2, String.valueOf(value2));
        for (Long id : ids) {
            delete(user, id);
        }
    }

    /**
     * Copies only files on file system by given attribute
     */
    @Override
    public void physicallyCopyFilesByAttr(String attribute, String value) throws IOException {
        logger.debug("copying files by attr: {} = {}", attribute, value);
        List<Map<String, Object>> res = daoutil.queryForMapList(this, "get_files_for_copy", attribute, value);
        for (Map<String, Object> r : res) {
            Long sourceId = (Long) r.get("source_id");
            String sourcePath = (String) r.get("source_path");
            Long targetId = (Long) r.get("target_id");
            String targetPath = (String) r.get("target_path");
            Path source = Paths.get(sourcePath + File.separator + sourceId);
            Path target = Paths.get(targetPath + File.separator + targetId);
            Files.copy(source, target);
            logger.debug("physically copied file from {} to {}", source, target);
        }
    }

    /**
     * Creates symbolic link of files by given attributes
     */
    @Override
    public void createSymlinksForFilesByAttr(String attribute, String value, String attribute2, String value2) throws IOException {
        List<Map<String, Object>> res =
                daoutil.queryForMapList(this, "get_files_for_public_access", attribute, value, attribute2, value2);

        String publicPath = conf.getPublicFileStorePath();
        EnumSet<PosixFilePermission> posixFilePermissions =
                EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE,
                        PosixFilePermission.OTHERS_READ);
        for (Map<String, Object> r : res) {
            Long fileId = (Long) r.get("file_id");
            String filePath = (String) r.get("path");
            Long targetId = (Long) r.get("file_id");
            Path source = Paths.get(filePath + File.separator + fileId);
            PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(source, PosixFileAttributeView.class);
            if (fileAttributeView != null) {
                Files.setPosixFilePermissions(source, posixFilePermissions);
                logger.debug("set posix permissions: permissions={}, file={}", posixFilePermissions, source);
                Path symblink = Paths.get(publicPath + File.separator + targetId);
                Files.createSymbolicLink(symblink, source);
                logger.debug("symblink {} is created for file {}", symblink, source);
            } else {
                logger.warn("not posix file system, can't create symlink");
                return;
            }
        }
    }
}
