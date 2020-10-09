package com.unifina.controller

import com.unifina.service.ENSService
import com.unifina.signalpath.blockchain.Web3jHelper
import grails.converters.JSON

class DevController {
	ENSService ensService

	@StreamrApi(authenticationLevel=AuthLevel.NONE)
	def foo() {
		String testdomain1 = Web3jHelper.getENSDomainOwner("testdomain1.eth")
		String testdomain2 = Web3jHelper.getENSDomainOwner("testdomain2.eth")
		String testdomain3 = Web3jHelper.getENSDomainOwner("testdomain3.eth")
		return render([
			testdomain1: testdomain1,
			testdomain2: testdomain2,
			testdomain3: testdomain3,
		] as JSON)
	}
}
