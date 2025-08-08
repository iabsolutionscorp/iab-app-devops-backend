package com.iab.devops.domain.enums;

public enum IACType {
    TERRAFORM(".tf", "text/plain; charset=utf-8");

    private final String fileExtension;
    private final String contentType;

    IACType(String fileExtension, String contentType) {
        this.fileExtension = fileExtension;
        this.contentType = contentType;
    }

    public String getArchiveSuffix() {
        return fileExtension;
    }

    public String getContentType() {
        return contentType;
    }
}
