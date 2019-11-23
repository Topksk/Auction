package com.bas.auction.docfiles.dto;

import com.bas.auction.core.dto.AuditableRow;
import com.bas.auction.core.json.ExcludeFromClient;
import com.bas.auction.core.json.ExcludeFromSearchIndex;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DocFile extends AuditableRow {
    /**
     *
     */
    private static final long serialVersionUID = -4567686570343489061L;

    @SerializedName("recid")
    private long fileId;
    private String fileName;
    @ExcludeFromClient
    private String fileType;
    private transient Long fileSize;
    private transient String path;
    private Long owner;
    private String ownerName;
    private String docType;
    private transient String hashValue;
    private transient Boolean isSystemGenerated;
    @ExcludeFromSearchIndex
    private Boolean canSign;
    @ExcludeFromSearchIndex
    private Boolean canRemove;
    private transient Long copiedFileId;
    @ExcludeFromSearchIndex
    private List<DocFileSignature> signatures;
    private Long tableId;
    private String tableName;

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public Path getFile() {
        String path = getPath() + File.separator + getFileId();
        return Paths.get(path);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<DocFileSignature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<DocFileSignature> fileSignatures) {
        this.signatures = fileSignatures;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Boolean isCanSign() {
        return canSign;
    }

    public void setCanSign(Boolean canSign) {
        this.canSign = canSign;
    }

    public Boolean isCanRemove() {
        return canRemove;
    }

    public void setCanRemove(Boolean canRemove) {
        this.canRemove = canRemove;
    }

    public Long getCopiedFileId() {
        return copiedFileId;
    }

    public void setCopiedFileId(Long copiedFileId) {
        this.copiedFileId = copiedFileId;
    }

    public Boolean getIsSystemGenerated() {
        return isSystemGenerated != null && isSystemGenerated;
    }

    public void setIsSystemGenerated(Boolean isSystemGenerated) {
        this.isSystemGenerated = isSystemGenerated;
    }

    @Override
    public String toString() {
        Path file = getFile();
        return "[file id: " + fileId + ", path = " + file.toString() + "]";
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fileId ^ (fileId >>> 32));
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
        DocFile other = (DocFile) obj;
        if (fileId != other.fileId)
            return false;
        return true;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


}
