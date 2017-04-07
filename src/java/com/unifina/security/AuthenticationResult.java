package com.unifina.security;

import com.unifina.domain.security.Key;
import com.unifina.domain.security.SecUser;

public class AuthenticationResult {
	private final Key key;
	private final SecUser secUser;
	private final boolean keyMissing;
	private final boolean lastAuthenticationMalformed;

	public AuthenticationResult(boolean keyMissing, boolean lastAuthenticationMalformed) {
		this.key = null;
		this.secUser = null;
		this.keyMissing = keyMissing;
		this.lastAuthenticationMalformed = lastAuthenticationMalformed;
	}

	public AuthenticationResult(SecUser secUser) {
		this.key = null;
		this.secUser = secUser;
		this.keyMissing = false;
		this.lastAuthenticationMalformed = false;
	}

	public AuthenticationResult(Key key) {
		this.secUser = key.getUser();
		this.key = key.getUser() != null ? null : key;
		this.keyMissing = false;
		this.lastAuthenticationMalformed = false;
	}

	public Key getKey() {
		return key;
	}

	public SecUser getSecUser() {
		return secUser;
	}

	public boolean isKeyMissing() {
		return keyMissing;
	}

	public boolean isLastAuthenticationMalformed() {
		return lastAuthenticationMalformed;
	}

	public boolean guarantees(AuthLevel level) {
		if (level == AuthLevel.USER) {
			return getSecUser() != null;
		} else if (level == AuthLevel.KEY) {
			return getSecUser() != null || getKey() != null;
		} else if (level == AuthLevel.NONE) {
			return true;
		} else {
			throw new RuntimeException("Unexpected authLevel: " + level);
		}
	}
}
