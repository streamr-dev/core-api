package com.unifina.signalpath.text;

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper
import javax.xml.bind.DatatypeConverter

public class DecodeStringToByteArraySpec extends Specification {
	void "Decode String To Byte Array works properly with default decode type base64"() {
		def module = new DecodeStringToByteArray()
		module.init()
		when:
		Map inputValues = [
			in: ['IghQAAAAAAAA']
		]
		Map outputValues = [
			out: [DatatypeConverter.parseBase64Binary('IghQAAAAAAAA')]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.customEquality { List expected, List actual -> Arrays.equals(expected, actual) }
			.test()
	}

	void "Decode String To Byte Array works properly with hex decode type"() {
		def module = new DecodeStringToByteArray()
		module.init()
		module.getInput('decodeType').receive('hex')
		when:
		Map inputValues = [
				in: ['0fb8']
		]
		Map outputValues = [
				out: [DatatypeConverter.parseHexBinary('0fb8')]
		]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.customEquality { List expected, List actual -> Arrays.equals(expected, actual) }
			.test()
	}
}