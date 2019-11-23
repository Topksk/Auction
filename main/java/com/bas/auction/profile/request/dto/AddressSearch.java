package com.bas.auction.profile.request.dto;

import java.util.Date;

/**
 * Created by Sauran.Alteyev on 12.06.2016.
 */
public class AddressSearch {
    String langId;
    String id;
    String city;
    String street;
    String building;
    String fraction;
    String phone;
    String kskDisp;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDisp() {
        return kskDisp;
    }

    public void setDisp(String kskDisp) {
        this.kskDisp = kskDisp;
    }

	public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



}
