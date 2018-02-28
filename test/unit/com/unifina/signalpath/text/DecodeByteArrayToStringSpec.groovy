package com.unifina.signalpath.text

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper
import javax.xml.bind.DatatypeConverter

class DecodeByteArrayToStringSpec extends Specification {

	def module

	def setup() {
		module = null
	}

	def cleanup() {

	}

	void "Decode Byte Array To String with default decode type hex"(){
		module = new DecodeByteArrayToString()
		module.init()
		when:
		Map inputValues = [
			'in': [DatatypeConverter.parseBase64Binary('IghQAAAAAAAA')]
		]
		Map outputValues = [
			'out': ['220850000000000000']
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "Decode Byte Array to String with base64 decode type"(){
		module = new DecodeByteArrayToString()
		module.init()
		module.getInput('decodeType').receive('base64')
		when:
		Map inputValues = [
				'in': [DatatypeConverter.parseBase64Binary('IghQAAAAAAAA')]
		]
		Map outputValues = [
				'out': ['IghQAAAAAAAA']
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
