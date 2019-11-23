package com.bas.auction.profile.employee.dto;

import com.bas.auction.core.dto.AuditableRow;

public class Person extends AuditableRow {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3233530059733157348L;
	private long personId;
	private String firstName;
	private String lastName;
	private String middleName;
	private String iin;
	private transient Boolean nonresident;

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getIin() {
		return iin;
	}

	public void setIin(String iin) {
		this.iin = iin;
	}

	public String getFullName() {
		StringBuilder sb = new StringBuilder(lastName);
		if (firstName != null)
			sb.append(' ').append(firstName);
		if (middleName != null)
			sb.append(' ').append(middleName);
		return sb.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (personId ^ (personId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (personId != other.personId)
			return false;
		return true;
	}

	public Boolean getNonresident() {
		return nonresident != null && nonresident;
	}

	public void setNonresident(Boolean nonresident) {
		this.nonresident = nonresident;
	}
}
