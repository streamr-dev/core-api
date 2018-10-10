package com.unifina.security;

import com.unifina.domain.security.SecUser;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;

import java.io.*;
import java.util.Base64;

public class SessionToken {
	private String token;
	private SecUser user;
	private DateTime expiration;

	public SessionToken(int tokenLength, SecUser user, int ttlHours) {
		this.token = RandomStringUtils.randomAlphanumeric(tokenLength);
		this.user = user;
		this.expiration = new DateTime().plusHours(ttlHours);
	}

	public String getToken() {
		return token;
	}

	public SecUser getUser() {
		return user;
	}

	public DateTime getExpiration() {
		return expiration;
	}
}
