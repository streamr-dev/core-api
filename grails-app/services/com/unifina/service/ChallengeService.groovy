package com.unifina.service

import com.unifina.domain.security.Challenge

class ChallengeService {
	def createChallenge() {
		def ch = new Challenge(challenge: "-")
		ch.save()
		ch.challenge = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n" + ch.id
		ch.save()
		return ch
	}
}
