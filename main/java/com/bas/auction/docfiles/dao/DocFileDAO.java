package com.bas.auction.docfiles.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.docfiles.dto.DocFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface DocFileDAO {
	DocFile findById(User user, Long fileId);

	List<DocFile> findByAttrWithoutProtocols(String attribute, String value);

	List<DocFile> findByAttr(User user, String attribute, Object value);

	List<DocFile> findByAttr(User user, String attribute, String value);

	List<DocFile> findByAttr(String attribute, String value);

	Long create(DocFile docFile);

	Long createNotifFile(DocFile docFile);

	Long create(String name, Long userId, Boolean isSystemGenerated, Path file, Map<String, String> attributes)
			throws IOException;

	boolean delete(User user, Long fileId) throws IOException;

	void deleteByAttr(User user, String attribute, Object value, boolean forceRemove) throws IOException;

	void deleteByAttr(User user, String attribute, Object value, String attribute2, Object value2) throws IOException;

	void physicallyCopyFilesByAttr(String attribute, String value) throws IOException;

	List<Long> findFileIdsByAttribute(String attribute, String value, String attribute2, String value2);

	boolean findIsFileExistsByAttribute(String attribute, String value, String attribute2, String value2);

	boolean findIsUnsignedFileExistsByAttribute(String attribute, String value);

	List<Map<String, Object>> findForIntegra(long negId);

	void createSymlinksForFilesByAttr(String attribute, String value, String attribute2, String value2) throws IOException;
}
