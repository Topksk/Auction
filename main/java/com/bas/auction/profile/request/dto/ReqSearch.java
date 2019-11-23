package com.bas.auction.profile.request.dto;

import java.util.Date;

/**
 * Created by Gabit.Omarov on 18.05.2016.
 */
public class ReqSearch {
    String id;
    String dat_reg_beg;
    String dat_reg_end;
    String req_city;
    String req_street;
    String req_building;
    String t_flats_id;
    String req_type;
    String t_req_subtype_id;
    String t_req_priority_id;
    String req_status;
    String req_disp_exec;
    String req_executer;
    String lang_id;
    String user_id;
    String ksk_id;
    String comp_id;

    public String getComp_id() {
        return comp_id;
    }

    public void setComp_id(String comp_id) {
        this.comp_id = comp_id;
    }

    public String getKsk_id() {
        return ksk_id;
    }

    public void setKsk_id(String ksk_id) {
        this.ksk_id = ksk_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDat_reg_beg() {
        return dat_reg_beg;
    }

    public void setDat_reg_beg(String dat_reg_beg) {
        this.dat_reg_beg = dat_reg_beg;
    }

    public String getDat_reg_end() {
        return dat_reg_end;
    }

    public void setDat_reg_end(String dat_reg_end) {
        this.dat_reg_end = dat_reg_end;
    }

    public String getReq_city() {
        return req_city;
    }

    public void setReq_city(String req_city) {
        this.req_city = req_city;
    }

    public String getReq_street() {
        return req_street;
    }

    public void setReq_street(String req_street) {
        this.req_street = req_street;
    }

    public String getReq_building() {
        return req_building;
    }

    public void setReq_building(String req_building) {
        this.req_building = req_building;
    }

    public String getT_flats_id() {
        return t_flats_id;
    }

    public void setT_flats_id(String t_flats_id) {
        this.t_flats_id = t_flats_id;
    }

    public String getReq_type() {
        return req_type;
    }

    public void setReq_type(String req_type) {
        this.req_type = req_type;
    }

    public String getT_req_subtype_id() {
        return t_req_subtype_id;
    }

    public void setT_req_subtype_id(String t_req_subtype_id) {
        this.t_req_subtype_id = t_req_subtype_id;
    }

    public String getT_req_priority_id() {
        return t_req_priority_id;
    }

    public void setT_req_priority_id(String t_req_priority_id) {
        this.t_req_priority_id = t_req_priority_id;
    }

    public String getReq_status() {
        return req_status;
    }

    public void setReq_status(String req_status) {
        this.req_status = req_status;
    }

    public String getReq_disp_exec() {
        return req_disp_exec;
    }

    public void setReq_disp_exec(String req_disp_exec) {
        this.req_disp_exec = req_disp_exec;
    }

    public String getReq_executer() {
        return req_executer;
    }

    public void setReq_executer(String req_executer) {
        this.req_executer = req_executer;
    }

    public String getLang_id() {
        return lang_id;
    }

    public void setLang_id(String lang_id) {
        this.lang_id = lang_id;
    }
}
