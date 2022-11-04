package com.kony.adminconsole.commons.exception;

/**
 * 
 * Exception class used for handling invalid file name while uploading a file
 * 
 * @author Alahari Akhil
 * @version 1.0
 */

public class InvalidFileNameException extends Exception {

    private static final long serialVersionUID = 8378581456282862039L;

    private String fileName;

    public InvalidFileNameException(Throwable cause, String fileName) {
        super(cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
