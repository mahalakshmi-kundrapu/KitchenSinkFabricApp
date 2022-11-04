package com.kony.service.util;

import java.util.Random;

/**
 * Class containing common Random Value Utility Methods
 *
 * @author Aditya Mankal
 */
public class RandomValueUtilities {

	private static Random rnd = new Random();

	public static long getNumericId() {
		long generatedValue;
		generatedValue = (long) 1000000000 + rnd.nextInt(900000000);
		return generatedValue;
	}

}
