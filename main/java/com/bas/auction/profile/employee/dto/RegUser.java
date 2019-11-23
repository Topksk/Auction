package com.bas.auction.profile.employee.dto;

import java.util.Date;

/**
 * Created by alibi.balgabekov on 05.05.2016.
 */
public class RegUser {
    String email;
    String password;
    String name;
    String surname;
    String midname;
    String iin;
    String mobilephone;
    String phone;
    Date birthdate;
    String city;
    String street;
    String home;
    String fraction;
    String flat;
    String fractflat;
    String fbpass;
    String sntrue;
    int relation;


    public String getSntrue() {
        return sntrue;
    }

    public void setSntrue(String sntrue) {
        this.sntrue = sntrue;
    }

    public String getFbpass() {
        return fbpass;
    }

    public void setFbpass(String fbpass) {
        this.fbpass = fbpass;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public String getFractflat() {
        return fractflat;
    }

    public void setFractflat(String fractflat) {
        this.fractflat = fractflat;
    }



    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }


    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }


    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }



    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMidname() {
        return midname;
    }

    public void setMidname(String midname) {
        this.midname = midname;
    }

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


}
