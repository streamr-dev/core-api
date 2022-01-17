package com.unifina.service;

public class ChallengeVerificationFailedException extends ApiException {
	public ChallengeVerificationFailedException(String message) {
		super(401, "CHALLENGE_VERIFICATION_FAILED_ERROR", message);
	}
}
