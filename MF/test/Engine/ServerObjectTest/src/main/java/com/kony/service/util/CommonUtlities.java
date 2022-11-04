package com.kony.service.util;

import java.util.Map;

import com.kony.service.constants.IdentityConstants;

/**
 * Class containing common Utility Methods
 *
 * @author Aditya Mankal
 */
public class CommonUtlities {

	/**
	 * Method to return the name of the Logged-In User
	 * 
	 * @return Logged In User-name
	 */
	public static String getLoggedInUsername() {
		String username = System.getProperty("user.name");
		return username;
	}

	/**
	 * Method to mask the X-Kony Auth Token value in the Map with the string "X-Kony-Auth-Token"
	 * 
	 * @param map
	 * @return Map without the X-Kony Auth Token
	 */
	public static Map<String, String> maskAuthTokenInMap(Map<String, String> map) {
		if (map == null || !map.containsKey(IdentityConstants.X_KONY_AUTHORIZATION)) {
			return map;
		}
		map.put(IdentityConstants.X_KONY_AUTHORIZATION, "X-Kony-Auth-Token");
		return map;
	}
}
