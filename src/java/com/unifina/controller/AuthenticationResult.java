package com.unifina.controller;

import com.unifina.domain.User;

public class AuthenticationResult {
	private final User user;
	private final boolean lastAuthenticationMalformed;
	private final boolean failed;

	public AuthenticationResult(boolean lastAuthenticationMalformed, boolean failed) {
		this.user = null;
		this.lastAuthenticationMalformed = lastAuthenticationMalformed;
		this.failed = failed;
	}

	public AuthenticationResult(User user) {
		this.user = user;
		this.lastAuthenticationMalformed = false;
		this.failed = false;
	}

	public User getSecUser() {
		return user;
	}

	public boolean isLastAuthenticationMalformed() {
		return lastAuthenticationMalformed;
	}

	public boolean guarantees(AuthLevel level) {
		if (level == AuthLevel.USER) {
			return !failed && getSecUser() != null;
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
