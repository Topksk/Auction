package com.bas.auction.docfiles.dao.impl;

import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dto.DocFileAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Repository
public class DocFileAttributeDAOImpl implements DocFileAttributeDAO, GenericDAO<DocFileAttribute> {
    private final static Logger logger = LoggerFactory.getLogger(DocFileAttributeDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public DocFileAttributeDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "file_attr";
    }

    @Override
    public Class<DocFileAttribute> getEntityType() {
        return DocFileAttribute.class;
    }

    @Override
    public List<DocFileAttribute> findFileAttributes(Long fileId) {
        return daoutil.query(this, "get_file_attrs", fileId);
    }

    @Override
    @SpringTransactional
    public void create(DocFileAttribute attr) {
        Object[] values = getInsertParams(attr);
        daoutil.insert(this, values);
    }

    @Override
    @SpringTransactional
    public void create(List<DocFileAttribute> attributes) {
        List<Object[]> values = attributes.stream().map(this::getInsertParams).collect(toList());
        daoutil.batchInsert(this, values);
    }

    private Object[] getInsertParams(DocFileAttribute attr) {
        return new Object[]{attr.getFileId(), attr.getName(), attr.getValue(), attr.getCreatedBy(), attr.getCreatedBy()};
    }

    @Override
    @SpringTransactional
    public void delete(String attr, String value, String attr2, String value2) {
        logger.debug("remove file attrs: {} = {}, {} = {}", attr, value, attr2, value2);
        Object[] params = {attr2, value2, attr, value};
        daoutil.delete(this, params);
    }

    @Override
    @SpringTransactional
    public void create(Long userId, Long fileId, String name, String value) {
        logger.debug("create file attrs: {} = {}", name, value);
        DocFileAttribute attr = new DocFileAttribute();
        attr.setFileId(fileId);
        attr.setName(name);
        attr.setValue(value);
        attr.setCreatedBy(userId);
        create(attr);
    }

    @Override
    @SpringTransactional
    public void create(Long userId, String attr, String value, String attr2, String value2) {
        logger.debug("create file attrs: {} = {}, {} = {}", attr, value, attr2, value2);
        delete(attr, value, attr2, value2);
        Object[] params = {attr2, value2, userId, userId, attr, value};
        daoutil.dml(this, "insert2", params);
    }

    @Override
    @SpringTransactional
    public void create(Long userId, String attr, Object value, String attr2, Object value2) {
        create(userId, attr, String.valueOf(value), attr2, String.valueOf(value2));
    }

    @Override
    @SpringTransactional
    public void delete(String attr, Object value, String attr2, Object value2) {
        delete(attr, String.valueOf(value), attr2, String.valueOf(value2));
    }

    @Override
    @SpringTransactional
    public void deleteFileAttrs(Long fileId) {
        Object[] values = {fileId};
        daoutil.dml(this, "delete_file_attrs", values);
    }

    @Override
    public Map<String, String> parse(String attributes) {
        String[] attrs = attributes.split("&");
        return Arrays.stream(attrs)
                .map(attr -> attr.split("="))
                .filter(pair -> pair.length == 2)
                .collect(toMap(pair -> pair[0], pair -> pair[1]));
    }

}
