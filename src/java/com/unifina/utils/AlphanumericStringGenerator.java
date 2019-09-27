package com.unifina.utils;

import org.apache.commons.lang.RandomStringUtils;

import java.security.SecureRandom;

public class AlphanumericStringGenerator {
	public static String getRandomAlphanumericString(int length) {
		return RandomStringUtils.random(length, 0, 0, true, true, null, new SecureRandom());
	}
}
