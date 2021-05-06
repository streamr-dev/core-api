package com.unifina.utils

import spock.lang.Specification

class ECRecoverSpec extends Specification {
	void "calculateMessageHash"() {
		expect:
		ECRecover.calculateMessageHash("This is a message") == [3, -124, 33, 45, -26, -43, 125, -28, 108, 97, 126, -118, -123, 5, 88, 9, -24, 59, 94, -85, 57, 66, 104, -7, -95, -84, 49, 36, -18, -128, 121, -41]
		ECRecover.calculateMessageHash("foobar") == [-32, 29, 56, -111, -36, -107, 30, 17, 56, 9, 119, 117, -25, 96, 110, -59, 114, 83, 87, -106, -109, -7, 62, -100, -115, -58, -76, 85, -32, -117, -121, -70]
	}

	void "recoverAddress"() {
		setup:
		byte[] messageHash = ECRecover.calculateMessageHash("This is a message")
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		expect:
		ECRecover.recoverAddress(messageHash, signature) == "0xc85be37e739a73ffc84026d01db0d4818f9902f6"
	}
}
