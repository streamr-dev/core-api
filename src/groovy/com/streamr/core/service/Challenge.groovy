package com.streamr.core.service

import com.streamr.core.utils.AlphanumericStringGenerator

import java.time.Instant
import java.time.format.DateTimeFormatter

class Challenge {
	private String id
	private String challenge
	private Date expiration

	Challenge(String text, int length, int ttlSeconds) {
		this.id = AlphanumericStringGenerator.getRandomAlphanumericString(length)
		this.challenge = text + id
		this.expiration = Date.from(Instant.now().plusSeconds(ttlSeconds))
	}

	Challenge(String id, String text, int ttlSeconds) {
		this.id = id
		this.challenge = text + id
		this.expiration = Date.from(Instant.now().plusSeconds(ttlSeconds))
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
