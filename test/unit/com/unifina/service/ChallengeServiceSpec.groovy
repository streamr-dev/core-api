package com.unifina.service

import com.unifina.domain.security.Challenge
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeService)
@Mock(Challenge)
class ChallengeServiceSpec extends Specification {
	void "should generate challenge"() {
		when:
		def challenge = service.createChallenge()
		def expected = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
		then:
		challenge.challenge.startsWith(expected)
		Challenge.count() == 1
	}

	void "response to challenge should pass - case 1"() {
		when:
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should pass - case 2"() {
		when:
		String address = "0x381caa291a8c96f4e1b18fe93dd1742c23e6a5a4"
		String signature = "0x16befea3fb9cc1863c669d8ba735094aa57b9382f618f68904a512b0f57a1b1c203b02836a70b223b26a3e6af0d24f138eab792379825ba44eccbdc7f19919541c"
		Challenge challenge = new Challenge(
			id: "q9iq56WVQN6_AjACbN-vHQzI6P4j99R8qUkMK61C6jHw",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\nq9iq56WVQN6_AjACbN-vHQzI6P4j99R8qUkMK61C6jHw"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should pass - case 3"() {
		when:
		String address = "0xa50e97f6a98dd992d9ecb8207c2aa58f54970729"
		String signature = "0x4df68b33c0282686a53a9dcb1e157ab7812d59bf30bfdbd2fcc115b1079644b6610bee3bc3b3f80d4b942f941c1667577fd84423ef3e0a79458abc510616acf51c"
		Challenge challenge = new Challenge(
			id: "21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q"
		).save(failOnError: true, validate: true)
		then:
		service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}

	void "response to challenge should fail"() {
		when:
		String address = "0x99a3ae3f5e713f01eca8c2bcf4c32702c2e7ea03"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
		).save(failOnError: true, validate: true)
		then:
		!service.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address)
	}
}
