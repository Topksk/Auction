package com.bas.auction.profile.employee.service;

import com.bas.auction.auth.dto.User;
import com.bas.auction.profile.employee.dto.Person;

public interface PersonService {
	Long findIdByIin(String iin);

	Person findById(long personId);

	Person create(User user, Person data);

	Person update(User user, Person data);
}
