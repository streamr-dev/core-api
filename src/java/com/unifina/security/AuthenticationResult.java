package com.unifina.security;

import com.unifina.domain.security.Key;
import com.unifina.domain.security.SecUser;

public class AuthenticationResult {
	private final Key key;
	private final SecUser secUser;
	private final boolean keyMissing;
	private final boolean lastAuthenticationMalformed;
	private final boolean failed;

	public AuthenticationResult(boolean keyMissing, boolean lastAuthenticationMalformed, boolean failed) {
		this.key = null;
		this.secUser = null;
		this.keyMissing = keyMissing;
		this.lastAuthenticationMalformed = lastAuthenticationMalformed;
		this.failed = failed;
	}

	public AuthenticationResult(SecUser secUser) {
		this.key = null;
		this.secUser = secUser;
		this.keyMissing = false;
		this.lastAuthenticationMalformed = false;
		this.failed = false;
	}

	public AuthenticationResult(Key key) {
		this.secUser = key.getUser();
		this.key = key.getUser() != null ? null : key;
		this.keyMissing = false;
		this.lastAuthenticationMalformed = false;
		this.failed = false;
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
			return !failed && getSecUser() != null;
		} else if (level == AuthLevel.KEY) {
			return !failed && (getSecUser() != null || getKey() != null);
		} else if (level == AuthLevel.NONE) {
			return !failed;
		} else {
			throw new RuntimeException("Unexpected authLevel: " + level);
		}
	}

	public boolean hasOneOfRoles(AllowRole[] roles) {
		for (AllowRole role : roles) {
			if (role.hasUserRole(getSecUser())) {
				return true;
			}
		}
		return false;
	}
}
