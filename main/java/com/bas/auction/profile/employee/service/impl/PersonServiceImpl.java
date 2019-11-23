package com.bas.auction.profile.employee.service.impl;

import com.bas.auction.auth.dto.User;
import com.bas.auction.auth.service.EmailValidationException;
import com.bas.auction.core.utils.validation.Validator;
import com.bas.auction.profile.employee.dao.PersonDAO;
import com.bas.auction.profile.employee.dto.Person;
import com.bas.auction.profile.employee.service.IinValidationException;
import com.bas.auction.profile.employee.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {
	private final PersonDAO personDAO;

	@Autowired
	public PersonServiceImpl(PersonDAO personDAO) {
		this.personDAO = personDAO;
	}

	@Override
	public Long findIdByIin(String iin) {
		return personDAO.findIdByIin(iin);
	}

	@Override
	public Person findById(long personId) {
		return personDAO.findById(personId);
	}

	@Override
	public Person create(User user, Person data) {
		boolean nonresident = data.getNonresident();
		if (!nonresident && !Validator.isValidIinOrBin(data.getIin()))
			throw new IinValidationException();
		if (nonresident && !Validator.isValidEmail(data.getIin()))
			throw new EmailValidationException();
		Long id = findIdByIin(data.getIin());
		if (id != null) {
			data.setPersonId(id);
			return data;
		}
		return personDAO.insert(user, data);
	}

	@Override
	public Person update(User user, Person data) {
		return personDAO.update(user, data);
	}

}
