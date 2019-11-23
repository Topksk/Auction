package com.bas.auction.auth.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by alibi.balgabekov on 09.05.2016.
 */
public class OneTimeCodeForEmail implements Serializable {

    private static final long serialVersionUID = 704828302276424420L;
    private String email;
    private String code;
    private Date activeFrom;
    private Date activeTo;

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(Date activeFrom) {
        this.activeFrom = activeFrom;
    }

    public Date getActiveTo() {
        return activeTo;
    }

    public void setActiveTo(Date activeTo) {
        this.activeTo = activeTo;
    }


}
