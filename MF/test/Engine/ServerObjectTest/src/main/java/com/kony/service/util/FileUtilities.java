package com.kony.service.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class containing common File Utility Methods
 *
 * @author Aditya Mankal
 */
public class FileUtilities {

	private static final Logger LOG = LogManager.getLogger(FileUtilities.class);

	/**
	 * Method to retrieve file contents as String
	 * 
	 * @param filePath
	 * @return fileContents
	 */
	public static String readFileContentsToString(String filePath) {
		try {
			return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			LOG.error("Exception in Reading File Contents from directory. File path:" + filePath, e);
		}
		return null;
	}

	public static String readFileFromClassPathToString(String filename) {
		try (InputStream inputStream = FileUtilities.class.getClassLoader()
				.getResourceAsStream(filename);) {
			return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			LOG.error("Exception in Reading File Contents from Classpath. File name:" + filename, e);
		}
		return null;
	}

	public static boolean writeStringToFile(String fileContent, String filePath) {
		try {
			FileUtils.writeStringToFile(new File(filePath), fileContent, StandardCharsets.UTF_8.name());
			return true;
		} catch (Exception e) {
			LOG.error("Exception in writing contents to File. File path:" + filePath, e);
			return false;
		}
	}
}
