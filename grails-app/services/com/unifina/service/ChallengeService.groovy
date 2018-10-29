package com.unifina.service

import com.unifina.api.ChallengeVerificationFailedException
import com.unifina.crypto.ECRecover
import com.unifina.security.Challenge

class ChallengeService {
	static final int TTL_SECONDS = 300
	static final int CHALLENGE_LENGTH = 30

	KeyValueStoreService keyValueStoreService

	Challenge createChallenge() {
		String challengeText = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
		Challenge ch = new Challenge(challengeText, CHALLENGE_LENGTH, TTL_SECONDS)
		keyValueStoreService.setWithExpiration(ch.id, ch.challenge, TTL_SECONDS)
		return ch
	}

	boolean verifyChallengeResponse(String challengeID, String challenge, String signature, String publicKey) {
		try {
			String address = verifyChallengeAndGetAddress(challengeID, challenge, signature)
			boolean valid = address.toLowerCase() == publicKey.toLowerCase()
			if (valid) {
				keyValueStoreService.delete(challengeID)
			}
			return valid
		} catch(ChallengeVerificationFailedException e) {
			return false
		}
	}

	String verifyChallengeAndGetAddress(String challengeID, String challenge, String signature) throws ChallengeVerificationFailedException {
		String challengeRedis = keyValueStoreService.get(challengeID)
		boolean invalidChallenge = challengeRedis == null || challenge != challengeRedis
		if (invalidChallenge) {
			throw new ChallengeVerificationFailedException("Invalid challenge: "+challengeID)
		}
		byte[] messageHash = ECRecover.calculateMessageHash(challenge)
		return ECRecover.recoverAddress(messageHash, signature)
	}
}
