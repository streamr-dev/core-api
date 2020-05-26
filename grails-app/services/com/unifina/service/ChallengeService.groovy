package com.unifina.service

import com.unifina.api.ChallengeVerificationFailedException
import com.unifina.crypto.ECRecover
import com.unifina.security.Challenge
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ChallengeService {
	static final int TTL_SECONDS = 300
	static final int CHALLENGE_LENGTH = 30
	private static final String textPart1 = "This is a challenge created by Streamr for address "
	private static final String textPart2 = " to prove private key ownership by signing this random data with it:\n\n"

	KeyValueStoreService keyValueStoreService

	Challenge createChallenge(String address) {
		Challenge ch = new Challenge(getChallengeText(address), CHALLENGE_LENGTH, TTL_SECONDS)
		keyValueStoreService.setWithExpiration(ch.id, ch.challenge, TTL_SECONDS)
		return ch
	}

	void checkValidChallengeResponse(String challengeID, String challenge, String signature, String givenAddress) throws ChallengeVerificationFailedException {
		String address = verifyChallengeAndGetAddress(challengeID, challenge, signature)
		boolean valid = address.toLowerCase() == givenAddress.toLowerCase() && challengeTextContainsAddress(challenge, givenAddress)
		if (valid) {
			keyValueStoreService.delete(challengeID)
		} else {
			throw new ChallengeVerificationFailedException("Invalid challenge")
		}
	}

	String verifyChallengeAndGetAddress(String challengeID, String challenge, String signature) throws ChallengeVerificationFailedException {
		String challengeRedis = keyValueStoreService.get(challengeID)
		boolean invalidChallenge = challengeRedis == null || challenge != challengeRedis
		if (invalidChallenge) {
			throw new ChallengeVerificationFailedException("Invalid challenge")
		}
		byte[] messageHash = ECRecover.calculateMessageHash(challenge)
		return ECRecover.recoverAddress(messageHash, signature)
	}

	private static String getChallengeText(String address) {
		return textPart1+address+textPart2
	}

	private static boolean challengeTextContainsAddress(String text, String address) {
		return text.substring(textPart1.length()).startsWith(address)
	}
}
