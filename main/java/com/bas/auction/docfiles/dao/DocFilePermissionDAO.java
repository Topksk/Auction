package com.bas.auction.docfiles.dao;

import java.util.List;

import com.bas.auction.auth.dto.User;
import com.bas.auction.docfiles.dto.DocFilePermission;

public interface DocFilePermissionDAO {

	void update(User user, List<DocFilePermission> perms);

	boolean[] findFilePermissions(Long fileId);

	List<DocFilePermission> findAll();

}
