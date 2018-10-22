package com.unifina.service

import com.unifina.security.Challenge
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(ChallengeService)
class ChallengeServiceSpec extends Specification {
	String text
	KeyValueStoreService keyValueStoreService

	void setup() {
		text = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
		keyValueStoreService = service.keyValueStoreService = Mock(KeyValueStoreService)
	}

	void "should generate challenge"() {
		when:
		Challenge challenge = service.createChallenge()
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
		challenge.getChallenge().startsWith(text)
		challenge.getId().length() == ChallengeService.CHALLENGE_LENGTH
	}

	void "response to challenge should pass - case 1"() {
		Challenge challenge = new Challenge("cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A", text, ChallengeService.TTL_SECONDS)
		when:
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		boolean valid = service.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
		valid
	}

	void "response to challenge should pass - case 2"() {
		Challenge challenge = new Challenge("q9iq56WVQN6_AjACbN-vHQzI6P4j99R8qUkMK61C6jHw", text, ChallengeService.TTL_SECONDS)
		when:
		String address = "0x381caa291a8c96f4e1b18fe93dd1742c23e6a5a4"
		String signature = "0x16befea3fb9cc1863c669d8ba735094aa57b9382f618f68904a512b0f57a1b1c203b02836a70b223b26a3e6af0d24f138eab792379825ba44eccbdc7f19919541c"
		boolean valid = service.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
		valid
	}

	void "response to challenge should pass - case 3"() {
		Challenge challenge = new Challenge("21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q", text, ChallengeService.TTL_SECONDS)
		when:
		String address = "0xa50e97f6a98dd992d9ecb8207c2aa58f54970729"
		String signature = "0x4df68b33c0282686a53a9dcb1e157ab7812d59bf30bfdbd2fcc115b1079644b6610bee3bc3b3f80d4b942f941c1667577fd84423ef3e0a79458abc510616acf51c"
		boolean valid = service.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		1 * keyValueStoreService.delete(challenge.getId())
		valid
	}

	void "response to challenge should fail"() {
		Challenge challenge = new Challenge("21kcVxd9Q8OLUA-NVZaUsQn6oULLwORBCK_h-Cp5vT7Q", text, ChallengeService.TTL_SECONDS)
		when:
		String address = "0x99a3ae3f5e713f01eca8c2bcf4c32702c2e7ea03"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		boolean valid = service.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		then:
		1 * keyValueStoreService.get(challenge.getId()) >> challenge.challenge
		0 * keyValueStoreService.delete(challenge.getId())
		!valid
	}
}
