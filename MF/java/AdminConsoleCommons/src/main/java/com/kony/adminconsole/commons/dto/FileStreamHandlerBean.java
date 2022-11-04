package com.kony.adminconsole.commons.dto;

import java.io.File;

public class FileStreamHandlerBean {

    private File fileObject;
    private String fileName;

    public File getFile() {
        return this.fileObject;
    }

    public void setFile(File file) {
        this.fileObject = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
