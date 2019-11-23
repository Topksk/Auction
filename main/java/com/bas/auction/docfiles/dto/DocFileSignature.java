package com.bas.auction.docfiles.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromClient;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class DocFileSignature extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = 7402571397608468289L;
    @SerializedName("recid")
    private long signatureId;
    @ExcludeFromSearchIndex
    private long fileId;
    private long userId;
    private long certificateId;
    @ExcludeFromClient
    private String signature;
    private Date signDate;
    private String fullName;

    public long getSignatureId() {
        return signatureId;
    }

    public void setSignatureId(long signatureId) {
        this.signatureId = signatureId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(long certificateId) {
        this.certificateId = certificateId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
