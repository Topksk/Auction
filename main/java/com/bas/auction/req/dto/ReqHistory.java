package com.bas.auction.req.dto;

import com.bas.auction.core.dto.AuditableRow;

import java.util.Date;

public class ReqHistory extends AuditableRow {

    private Long t_request_id;
    private Date d_history;
    private Integer t_position_id;
    private Integer t_req_status_id;
    private String t_note;
    private String exec_Email;
    private Long id;
    private Integer sid;
    private Integer disp_id;
    private String crReqMail;
    private Long user_id;


    public Long getT_request_id() {
        return t_request_id;
    }

    public Date getD_history() {
        return d_history;
    }

    public void setD_history(Date d_history) {
        this.d_history = d_history;
    }

    public void setT_request_id(Long t_request_id) {
        this.t_request_id = t_request_id;
    }

    public Integer getT_position_id() {
        return t_position_id;
    }

    public void setT_position_id(Integer t_position_id) {
        this.t_position_id = t_position_id;
    }


    public Integer getT_req_status_id() {
        return t_req_status_id;
    }

    public void setT_req_status_id(Integer t_req_status_id) {
        this.t_req_status_id = t_req_status_id;
    }

    public String getT_note() {
        return t_note;
    }

    public void setT_note(String t_note) {
        this.t_note = t_note;
    }

    public String getExec_Email() {
        return exec_Email;
    }

    public void setExec_Email(String exec_Email) {
        this.exec_Email = exec_Email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public Integer getDisp_id() {
        return disp_id;
    }

    public void setDisp_id(Integer disp_id) {
        this.disp_id = disp_id;
    }

    public String getCrReqMail() {
        return crReqMail;
    }

    public void setCrReqMail(String crReqMail) {
        this.crReqMail = crReqMail;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
}
