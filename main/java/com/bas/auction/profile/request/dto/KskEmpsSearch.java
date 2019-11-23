package com.bas.auction.profile.request.dto;

import java.util.Date;

/**
 * Created by Sauran.Alteyev on 12.06.2016.
 */
public class KskEmpsSearch {
    String langId;
    String id;
    String lastName;
    String firstName;
    String middleName;
    String priznPos;
    String phone;
	String status;



    public String getLangId() {
        return langId;
    }

    public void setLangId(String langId) {
        this.langId = langId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPriznPos() {
        return priznPos;
    }

    public void setPriznPos(String priznPos) {
        this.priznPos = priznPos;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

	public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
