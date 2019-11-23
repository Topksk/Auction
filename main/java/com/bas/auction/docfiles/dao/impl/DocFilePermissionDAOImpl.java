package com.bas.auction.docfiles.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.core.spring.SpringTransactional;
import com.bas.auction.docfiles.dao.DocFileAttributeDAO;
import com.bas.auction.docfiles.dao.DocFilePermissionDAO;
import com.bas.auction.docfiles.dto.DocFileAttribute;
import com.bas.auction.docfiles.dto.DocFilePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DocFilePermissionDAOImpl implements DocFilePermissionDAO, GenericDAO<DocFilePermission> {
	private final DocFileAttributeDAO docFileAttributeDAO;
	private final DaoJdbcUtil daoutil;

	@Autowired
	public DocFilePermissionDAOImpl(DaoJdbcUtil daoutil, DocFileAttributeDAO docFileAttributeDAO) {
		this.daoutil = daoutil;
		this.docFileAttributeDAO = docFileAttributeDAO;
	}

	@Override
	public String getSqlPath() {
		return "file_perms";
	}

	@Override
	public Class<DocFilePermission> getEntityType() {
		return DocFilePermission.class;
	}

	@Override
	public boolean[] findFilePermissions(Long fileId) {
		boolean[] r = { true, true, true, true, true, true };
		List<DocFilePermission> permList = findAll();
		List<DocFileAttribute> fa = docFileAttributeDAO.findFileAttributes(fileId);
		for (DocFilePermission perm : permList) {
			for (DocFileAttribute attr : fa) {
				if (attr.getName().equals(perm.getAttribute())) {
					r[0] = r[0] && perm.isCustomerRead();
					r[1] = r[1] && perm.isCustomerSign();
					r[2] = r[2] && perm.isCustomerRemove();
					r[3] = r[3] && perm.isSupplierRead();
					r[4] = r[4] && perm.isSupplierSign();
					r[5] = r[5] && perm.isSupplierRemove();
					break;
				}
			}
		}
		return r;
	}

    @Override
    public List<DocFilePermission> findAll() {
        return daoutil.query(this, "get_all");
    }

	@Override
    @SpringTransactional
    public void update(User user, List<DocFilePermission> perms) {
        List<Object[]> values = perms.stream()
                .map(perm -> mapToUpdateParams(user, perm))
                .collect(Collectors.toList());
        daoutil.batchUpdate(this, values);
    }

    private Object[] mapToUpdateParams(User user, DocFilePermission perm) {
        return new Object[]{perm.getAttribute(), perm.getValue(), perm.isCustomerRead(), perm.isCustomerSign(),
                perm.isCustomerRemove(), perm.isSupplierRead(), perm.isSupplierSign(), perm.isSupplierRemove(),
                user.getUserId(), perm.getPermissionId()};
    }
}
