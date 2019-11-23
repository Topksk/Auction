package com.bas.auction.docfiles.dao;

import com.bas.auction.docfiles.dto.DocFileSignature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DocFileSignatureDAO {
	byte[] findSignatureBody(Long signId);

	void create(DocFileSignature fileSignature);

	void create(List<DocFileSignature> fileSignatures);

	void create(Long fileId, Long certId, Path file) throws IOException;

	List<DocFileSignature> findFileSignatures(Long fileId);

	void deleteFileSignatures(Long fileId);
}
