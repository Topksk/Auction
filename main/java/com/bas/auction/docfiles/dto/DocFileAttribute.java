package com.bas.auction.docfiles.dto;

import com.bas.auction.core.dto.AuditableRow;

import java.util.Arrays;

public class DocFileAttribute extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -32807166817546907L;

    private Long attributeId;
    private Long fileId;
    private String name;
    private String value;

    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNegId() {
        return "neg_id".equals(name);
    }

    public boolean isBidId() {
        return "bid_id".equals(name);
    }

    public boolean isReadOnly() {
        return "read_only".equals(name);
    }

    public boolean isBidReport() {
        return "file_type".equals(name) && "BID_REPORT".equals(value);
    }

    public boolean isBidParticipationReport() {
        return "file_type".equals(name) && "BID_PARTICIPATION_APPL".equals(value);
    }

    public boolean isNegPublishReport() {
        return "file_type".equals(name) && "NEG_PUBLISH_REPORT".equals(value);
    }

    public boolean isNegResumeReport() {
        return "file_type".equals(name) && "NEG_RESUME_REPORT".equals(value);
    }

    public boolean isNegProtocol() {
        return "file_type".equals(name) &&
                Arrays.asList("NEG_PUBLISH_REPORT", "NEG_OPENING_REPORT", "NEG_RESUME_REPORT",
                "NEG_OPENING_REPORT_STAGE1", "NEG_PUBLISH_REPORT_STAGE1", "NEG_RESUME_REPORT_STAGE1").contains(value);
    }

    public boolean isBidProtocol() {
        return "file_type".equals(name) &&
                Arrays.asList("BID_REPORT", "BID_PARTICIPATION_APPL").contains(value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (attributeId ^ (attributeId >>> 32));
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
        DocFileAttribute other = (DocFileAttribute) obj;
        return !attributeId.equals(other.attributeId);
    }

    @Override
    public String toString() {
        return "id: " + attributeId + ", fileId: " + fileId + ", name: " + name +
                ",value: " + value;
    }
}
