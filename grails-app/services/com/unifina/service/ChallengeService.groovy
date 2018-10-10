package com.unifina.service

import com.unifina.crypto.ECRecover
import com.unifina.domain.security.Challenge

class ChallengeService {
	def createChallenge() {
		def ch = new Challenge(challenge: "-")
		ch.save()
		ch.challenge = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n" + ch.id
		ch.save()
		return ch
	}

	boolean verifyChallengeResponse(String challengeID, String challenge, String signature, String publicKey) {
		Challenge dbChallenge = Challenge.get(challengeID)
		boolean invalidChallenge = dbChallenge == null || challenge != dbChallenge.challenge
		if (invalidChallenge) {
			return false
		}
		byte[] messageHash = ECRecover.calculateMessageHash(challenge)
		String address = ECRecover.recoverAddress(messageHash, signature)
		boolean valid = address == publicKey
		if (valid) {
			dbChallenge.delete()
		}
		return valid
	}
}
