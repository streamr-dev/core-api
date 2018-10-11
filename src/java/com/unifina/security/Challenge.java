package com.unifina.security;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;

import java.util.Date;

public class Challenge {
	private String id;
	private String challenge;
	private Date expiration;

	public Challenge(String text, int length, int ttlSeconds) {
		this.id = RandomStringUtils.randomAlphanumeric(length);
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
}
