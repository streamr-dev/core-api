package com.unifina.service;

import com.unifina.domain.Userish;
import com.unifina.utils.AlphanumericStringGenerator;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SessionToken {
	private final static String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static TimeZone UTC = TimeZone.getTimeZone("UTC");

	private final String token;
	private final Userish user;
	private final Date expiration;

	public SessionToken(int tokenLength, Userish user, int ttlHours) {
		this.token = AlphanumericStringGenerator.getRandomAlphanumericString(tokenLength);
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
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		df.setTimeZone(UTC);

		Map<String, Object> map = new HashMap<>();
		map.put("token", token);
		map.put("expires", df.format(expiration));
		return map;
	}
}
