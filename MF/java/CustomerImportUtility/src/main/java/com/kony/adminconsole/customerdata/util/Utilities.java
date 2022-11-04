package com.kony.adminconsole.customerdata.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.kony.adminconsole.customerdata.crypto.BCrypt;

public class Utilities {

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(11));
    }

    /**
     * Method to generate a Random Id
     * 
     * @return String
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Method to extract the File Extension of a file
     * 
     * @param filePointer
     * @return String
     */
    public static String getFileExtension(File filePointer) {
        if (filePointer == null || !filePointer.exists())
            return null;
        String fileName = filePointer.getName();
        if (StringUtils.isBlank(fileName) || !fileName.contains("."))
            return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Method to get a CSV Record as a List
     * 
     * @param record
     * @return List<String>
     */
    public static List<String> getRecordAsList(CSVRecord record) {
        List<String> recordList = new ArrayList<String>();
        for (int indexVar = 0; indexVar < record.size(); indexVar++) {
            recordList.add(record.get(indexVar));
        }
        return recordList;
    }
}
