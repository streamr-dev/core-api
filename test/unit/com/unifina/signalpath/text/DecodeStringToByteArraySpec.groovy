package com.unifina.signalpath.text;

import spock.lang.Specification

import com.unifina.utils.testutils.ModuleTestHelper
import javax.xml.bind.DatatypeConverter

public class DecodeStringToByteArraySpec extends Specification {

	def module

	def setup() {
		module = null
	}

	def cleanup() {

	}

	void "Decode String To Byte Array works properly with default decode type base64"() {
		module = new DecodeStringToByteArray()
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
		.customEquality { expected, actual ->
			Arrays.equals(expected, actual)
		}
		.test()
	}

	void "Decode String To Byte Array works properly with hex decode type"() {
		module = new DecodeStringToByteArray()
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
				.customEquality { expected, actual ->
			Arrays.equals(expected, actual)
		}
		.test()
	}
}