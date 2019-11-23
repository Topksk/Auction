package com.bas.auction.neg.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.neg.dao.NegTeamDAO;
import com.bas.auction.neg.dto.NegTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;

@Repository
public class NegTeamDAOImpl implements NegTeamDAO, GenericDAO<NegTeam> {
    private final static Logger logger = LoggerFactory.getLogger(NegTeamDAOImpl.class);
    private final DaoJdbcUtil daoutil;

    @Autowired
    public NegTeamDAOImpl(DaoJdbcUtil daoutil) {
        this.daoutil = daoutil;
    }

    @Override
    public String getSqlPath() {
        return "negotiations/team";
    }

    @Override
    public Class<NegTeam> getEntityType() {
        return NegTeam.class;
    }

    @Override
    public List<NegTeam> findNegTeam(Long negId) {
        logger.debug("get neg team: {}", negId);
        return daoutil.query(this, "list", negId);
    }

    @Override
    public boolean findIsNegOwnerOrganizer(Long negId) {
        return daoutil.exists(this, "is_neg_owner_organizer", negId);
    }

    private boolean exists(Long negId, Long userId) {
        return daoutil.exists(this, "exists", negId, userId);
    }

    @Override
    public NegTeam create(User user, NegTeam data) {
        Object[] values = mapToInsertValues(user, data);
        daoutil.insert(this, values);
        return data;
    }

    @Override
    public void copyTeam(User user, Long sourceNegId, Long destinationNegId) {
        Object[] values = {destinationNegId, user.getUserId(), user.getUserId(), sourceNegId};
        daoutil.dml(this, "copy_team", values);
    }

    @Override
    public void upsert(User user, Long negId, List<NegTeam> members) {
        if (members == null)
            return;
        members.forEach(member -> member.setNegId(negId));
        Map<Boolean, List<NegTeam>> membersPartitionedByExistence = members.stream()
                .collect(partitioningBy(member -> exists(negId, member.getUserId())));
        List<Object[]> insertParams = membersPartitionedByExistence.get(Boolean.FALSE).stream()
                .map(member -> mapToInsertValues(user, member))
                .collect(toList());
        List<Object[]> updateParams = membersPartitionedByExistence.get(Boolean.TRUE).stream()
                .map(member -> mapToUpdateValues(user, member))
                .collect(toList());
        if (!insertParams.isEmpty())
            daoutil.batchInsert(this, insertParams);
        if (!updateParams.isEmpty())
            daoutil.batchUpdate(this, updateParams);
    }

    private Object[] mapToInsertValues(User user, NegTeam member) {
        return new Object[]{member.getNegId(), member.getUserId(), member.getMemberPosition(), member.getRoleCode(),
                user.getUserId(), user.getUserId()};
    }

    private Object[] mapToUpdateValues(User user, NegTeam member) {
        return new Object[]{member.getMemberPosition(), member.getRoleCode(), user.getUserId(), member.getNegId(),
                member.getUserId()};
    }

    @Override
    public void delete(Long negId, List<Long> data) {
        if (data == null)
            return;
        logger.debug("remove neg team members: negId = {}, members = {}", negId, data);
        List<Object[]> values = data.stream()
                .map(id -> mapToDeleteValues(negId, id))
                .collect(toList());
        daoutil.batchDelete(this, values);
    }

    private Object[] mapToDeleteValues(Long negId, Long id) {
        return new Object[]{negId, id};
    }

    @Override
    public void deleteNegTeam(Long negId) {
        logger.debug("remove neg team members: negId = {}", negId);
        daoutil.dml(this, "delete_neg_team", new Object[]{negId});
    }
}
