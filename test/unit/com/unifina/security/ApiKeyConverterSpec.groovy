package com.unifina.security

import spock.lang.Specification

class ApiKeyConverterSpec extends Specification {
	void "createEthereumPrivateKey"() {
		when:
		String converted = ApiKeyConverter.createEthereumPrivateKey("mock-api-key")
		then:
		true
		converted == "372da4ca3dbb6829ddbaaf81478344e89c1544d8be98705c54477de238e448b7"
	}
}
