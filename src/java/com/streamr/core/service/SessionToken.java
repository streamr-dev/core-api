package com.streamr.core.service;

import com.streamr.core.domain.User;
import com.streamr.core.utils.AlphanumericStringGenerator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SessionToken {
	private final String token;
	private final User user;
	private final ZonedDateTime expiration;

	public SessionToken(int tokenLength, User user, int ttlHours) {
		this.token = AlphanumericStringGenerator.getRandomAlphanumericString(tokenLength);
		this.user = user;
		this.expiration = ZonedDateTime.now().plusHours(ttlHours);
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
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
