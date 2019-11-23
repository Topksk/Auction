package com.bas.auction.profile.supplier.dto;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SupplierSetting {

    @SerializedName("recid")
    private long settingId;
    private long supplierId;
    private String name;
    private List<NegNotification> notifications;

    public long getSettingId() {
        return settingId;
    }

    public void setSettingId(long settingId) {
        this.settingId = settingId;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NegNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NegNotification> notifications) {
        this.notifications = notifications;
    }
}
