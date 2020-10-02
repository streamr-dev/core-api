package com.unifina.service;

import com.unifina.domain.Userish;
import com.unifina.utils.AlphanumericStringGenerator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionToken {
	private final String token;
	private final Userish user;
	private final ZonedDateTime expiration;

	public SessionToken(int tokenLength, Userish user, int ttlHours) {
		this.token = AlphanumericStringGenerator.getRandomAlphanumericString(tokenLength);
		this.user = user;
		this.expiration = ZonedDateTime.now().plusHours(ttlHours);
	}

	public String getToken() {
		return token;
	}

	public Userish getUserish() {
		return user;
	}

	public Date getExpiration() {
		return Date.from(expiration.toInstant());
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("token", token);
		map.put("expires", expiration.format(DateTimeFormatter.ISO_INSTANT));
		return map;
	}
}
