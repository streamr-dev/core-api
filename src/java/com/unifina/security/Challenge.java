package com.unifina.security;

import com.unifina.utils.AlphanumericStringGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Challenge {
	private String id;
	private String challenge;
	private Date expiration;

	private static DateFormat df;
	private static DateFormat getDateFormat() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return df;
	}

	public Challenge(String text, int length, int ttlSeconds) {
		this.id = AlphanumericStringGenerator.getRandomAlphanumericString(length);
		this.challenge = text + id;
		this.expiration = new DateTime().plusSeconds(ttlSeconds).toDate();
	}

	public Challenge(String id, String text, int ttlSeconds) {
		this.id = id;
		this.challenge = text + id;
		this.expiration = new DateTime().plusSeconds(ttlSeconds).toDate();
	}

	public String getId() {
		return id;
	}

	public String getChallenge() {
		return challenge;
	}

	public Date getExpiration() {
		return expiration;
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("challenge", challenge);
		map.put("expires", getDateFormat().format(expiration));
		return map;
	}
}
