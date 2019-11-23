package com.bas.auction.docfiles.dao;

import com.bas.auction.docfiles.dto.DocFileAttribute;

import java.util.List;
import java.util.Map;

public interface DocFileAttributeDAO {

	List<DocFileAttribute> findFileAttributes(Long fileId);

	void create(DocFileAttribute attr);

	void create(Long userId, Long fileId, String attr, String value);

	void create(List<DocFileAttribute> attributes);

	void delete(String attr, String value, String attr2, String value2);

	void delete(String attr, Object value, String attr2, Object value2);

	void deleteFileAttrs(Long fileId);

	void create(Long userId, String attr, String value, String attr2, String value2);

	void create(Long userId, String attr, Object value, String attr2, Object value2);

	Map<String, String> parse(String attributes);
}
