package com.bas.auction.auth.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromClient;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class User extends AuditableRow implements UserDetails {
    /**
     *
     */
    private static final long serialVersionUID = 8438934675308144063L;
    @SerializedName("recid")
    private Long userId;
    private String login;
    private String email;
    private Long personId;
    private Long supplierId;
    private Long customerId;
    private String phoneNumber;
    private Date birthday;
    private String iin;
    private String name;
    private String surname;
    private String midname;
    private String mobilePhone;
    private Boolean active;
    private Boolean mainUser;
    private transient String password;
    private int city;
    private int street;
    private String fraction;
    private String fractflat;
    private int home;
    private int flat;
    int relation;
    String fbpass;
    String sntrue;

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

    public String getFraction() {
        return fraction;
    }

    public void setFraction(String fraction) {
        this.fraction = fraction;
    }

    public int getCity() {
        return city;
    }

    public void setCity(int city) {
        this.city = city;
    }

    public int getStreet() {
        return street;
    }

    public void setStreet(int street) {
        this.street = street;
    }

    public int getFlat() {
        return flat;
    }

    public void setFlat(int flat) {
        this.flat = flat;
    }

    public int getHome() {
        return home;
    }

    public void setHome(int home) {
        this.home = home;
    }

    @ExcludeFromClient
    @ExcludeFromSearchIndex
    private Boolean sysadmin;

    private String userPosition;



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

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }


    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }



    public static User sysadmin() {
        User user = new User(1L);
        user.sysadmin = true;
        return user;
    }

    public User() {

    }

    public User(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean isActive() {
        return active != null && active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public boolean isCustomer() {
        return customerId != null && customerId > 0;
    }

    public boolean isSupplier() {
        return supplierId != null && supplierId > 0;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public boolean isMainUser() {
        return mainUser != null && mainUser;
    }

    public boolean isMainUserOrSysadmin() {
        return isMainUser() || isSysadmin();
    }

    public void setMainUser(Boolean mainUser) {
        this.mainUser = mainUser;
    }

    public Boolean isSysadmin() {
        return sysadmin != null && sysadmin;
    }

    public void setSysadmin(Boolean sysadmin) {
        this.sysadmin = sysadmin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (userId ^ (userId >>> 32));
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
        User other = (User) obj;
        return userId.equals(other.userId);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (isSysadmin())
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isCustomer())
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        if (isSupplier())
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPPLIER"));
        if (isMainUser())
            authorities.add(new SimpleGrantedAuthority("ROLE_MAIN_USER"));
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive();
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }

}
