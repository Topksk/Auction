package com.bas.auction.neg.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.neg.dto.NegTeam;

import java.util.List;

public interface NegTeamDAO {
	List<NegTeam> findNegTeam(Long negId);

	void delete(Long negId, List<Long> data);

	boolean findIsNegOwnerOrganizer(Long negId);

	NegTeam create(User user, NegTeam data);

	void copyTeam(User user, Long sourceNegId, Long destinationNegId);

	void upsert(User user, Long negId, List<NegTeam> members);

	void deleteNegTeam(Long negId);
}
