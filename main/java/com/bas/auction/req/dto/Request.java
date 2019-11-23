package com.bas.auction.req.dto;

import com.bas.auction.core.dto.AuditableRow;
import java.util.Date;

public class Request extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -8832424373200038843L;



    private Integer crReqSubtype;
    private Integer crReqPriority;
    private String crReqDateExec;
    private Integer crReqFlat;
    private String crNote;
    private Long crUser;
    private Long crReqId;
    private String crUserMail;




    public Integer getCrReqSubtype() {
        return crReqSubtype;
    }

    public void setCrReqSubtype(Integer crReqSubtype) {
        this.crReqSubtype = crReqSubtype;
    }

    public Integer getCrReqPriority() {
        return crReqPriority;
    }

    public void setCrReqPriority(Integer crReqPriority) {
        this.crReqPriority = crReqPriority;
    }

    public String getCrReqDateExec() {
        return crReqDateExec;
    }

    public void setCrReqDateExec(String crReqDateExec) {
        this.crReqDateExec = crReqDateExec;
    }

    public Integer getCrReqFlat() {
        return crReqFlat;
    }

    public void setCrReqFlat(Integer crReqFlat) {
        this.crReqFlat = crReqFlat;
    }

    public String getCrNote() {
        return crNote;
    }

    public void setCrNote(String crNote) {
        this.crNote = crNote;
    }

    public Long getCrUser() {
        return crUser;
    }

    public void setCrUser(Long crUser) {
        this.crUser = crUser;
    }

    public Long getCrReqId() {
        return crReqId;
    }

    public void setCrReqId(Long crReqId) {
        this.crReqId = crReqId;
    }

    public String getCrUserMail() {
        return crUserMail;
    }

    public void setCrUserMail(String crUserMail) {
        this.crUserMail = crUserMail;
    }



}
