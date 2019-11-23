package com.bas.auction.profile.employee.dao;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.employee.dto.Person;

public interface PersonDAO {

	Long findIdByIin(String iin);

	Person findById(long personId);

	Person insert(User user, Person data);

	Person update(User user, Person data);
}
