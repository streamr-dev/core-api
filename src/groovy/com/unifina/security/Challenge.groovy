package com.unifina.security

import com.unifina.utils.AlphanumericStringGenerator
import org.joda.time.DateTime

import java.time.format.DateTimeFormatter

class Challenge {
	private String id
	private String challenge
	private Date expiration

	Challenge(String text, int length, int ttlSeconds) {
		this.id = AlphanumericStringGenerator.getRandomAlphanumericString(length)
		this.challenge = text + id
		this.expiration = new DateTime().plusSeconds(ttlSeconds).toDate()
	}

	Challenge(String id, String text, int ttlSeconds) {
		this.id = id
		this.challenge = text + id
		this.expiration = new DateTime().plusSeconds(ttlSeconds).toDate()
	}

	public String getId() {
		return id
	}

	public String getChallenge() {
		return challenge;
	}

	public Date getExpiration() {
		return expiration;
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<String, Object>()
		map.put("id", id)
		map.put("challenge", challenge)
		map.put("expires", DateTimeFormatter.ISO_INSTANT.format(expiration.toInstant()))
		return map
	}
}
