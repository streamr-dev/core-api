package com.unifina.security;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SessionToken {
	private String token;
	private Userish user;
	private Date expiration;

	private static DateFormat df;
	private static DateFormat getDateFormat() {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		return df;
	}

	public SessionToken(int tokenLength, Userish user, int ttlHours) {
		this.token = RandomStringUtils.random(tokenLength, 0, 0, true, true, null, new SecureRandom());
		this.user = user;
		this.expiration = new DateTime().plusHours(ttlHours).toDate();
	}

	public String getToken() {
		return token;
	}

	public Userish getUserish() {
		return user;
	}

	public Date getExpiration() {
		return expiration;
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("token", token);
		map.put("expires", getDateFormat().format(expiration));
		return map;
	}
}
