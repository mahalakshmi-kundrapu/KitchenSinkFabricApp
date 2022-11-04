package com.kony.service.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class containing common Date Utility Methods
 *
 * @author Aditya Mankal
 */
public class DateUtilities {

	public static String getDateAsString(Date date, String dateFormat) {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(date);
	}

}
