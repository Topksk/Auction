package com.bas.auction.profile.employee.dao.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.core.dao.DaoJdbcUtil;
import com.bas.auction.core.dao.GenericDAO;
import com.bas.auction.profile.employee.dao.PersonDAO;
import com.bas.auction.profile.employee.dto.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PersonDAOImpl implements PersonDAO, GenericDAO<Person> {
	private final DaoJdbcUtil daoutil;

	@Autowired
	public PersonDAOImpl(DaoJdbcUtil daoutil) {
		this.daoutil = daoutil;
	}

	@Override
	public String getSqlPath() {
		return "person";
	}

	@Override
	public Class<Person> getEntityType() {
		return Person.class;
	}

	@Override
	public Long findIdByIin(String iin) {
		return daoutil.queryScalar(this, "get_id_by_iin", iin);
	}

	@Override
	public Person findById(long personId) {
		return daoutil.queryForObject(this, "get", personId);
	}

	@Override
	public Person insert(User user, Person data) {
		Object[] values = { data.getFirstName(), data.getLastName(), data.getMiddleName(), data.getIin(),
				user.getUserId(), user.getUserId() };
		KeyHolder kh = daoutil.insert(this, values);
		data.setPersonId((long) kh.getKeys().get("person_id"));
		return data;
	}

	@Override
	public Person update(User user, Person data) {
		Object[] values = { data.getFirstName(), data.getLastName(), data.getMiddleName(), user.getUserId(),
				data.getPersonId() };
		daoutil.update(this, values);
		return data;
	}
}
