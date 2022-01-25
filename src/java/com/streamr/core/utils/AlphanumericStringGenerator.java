package com.streamr.core.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public final class AlphanumericStringGenerator {
	private static final SecureRandom secureRandom = new SecureRandom();

	private AlphanumericStringGenerator() {
	}

	public static String getRandomAlphanumericString(int length) {
		return RandomStringUtils.random(length, 0, 0, true, true, null, secureRandom);
	}
}
