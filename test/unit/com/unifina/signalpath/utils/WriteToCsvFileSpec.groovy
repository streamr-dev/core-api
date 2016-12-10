package com.unifina.signalpath.utils

import com.unifina.utils.testutils.FakeFileHolder
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class WriteToCsvFileSpec extends Specification {

	FakeFileHolder fakeFileHolder = new FakeFileHolder()
	WriteToCsvFile module

	def setup() {
		module = new WriteToCsvFile(fakeFileHolder)
		module.init()
		module.getInput("input-1")
		module.getInput("input-2")
		module.getInput("input-3")
		module.getInput("input-1").setDisplayName("firstInput")
		module.getInput("input-2").setDisplayName("secondInput")
	}

	private boolean testForFileContent(String s) {
		Map inputValues = [
			firstInput : ["first", "second", "third"],
			secondInput: [1, 2, 3]*.doubleValue(),
			in3        : [true, false, false]
		]
		Map outputValues = [:]
		Map channelMessages = [
			uiChannelId: [
				[type: "csvRowCount", value: 2l],
				[type: "csvRowCount", value: 3l],
				[type: "csvRowCount", value: 4l],
				[type: "csvFileReady", file: "test.csv", kilobytes: 666l]
			]
		]

		ModuleTestHelper moduleTestHelper
		def builder = new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(60 * 1000)
			.uiChannelMessages(channelMessages)
			.afterEachTestCase {
				String fileContents = fakeFileHolder.resolveStringsAndReset()
				assert fileContents == s
			}
		moduleTestHelper = builder.build()
		return moduleTestHelper.test()
	}

	void "WriteToCsv works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"]
		])

		then:
		testForFileContent(
			"timestamp\tfirstInput\tsecondInput\n" +
			"0\tfirst\t1.0\n" +
			"60000\tsecond\t2.0\n" +
			"120000\tthird\t3.0\n"
		)
	}

	void "WriteToCsv, with header writing disabled, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
				writeHeader: [value: false]
			]
		])

		then:
		testForFileContent(
			"0\tfirst\t1.0\n" +
			"60000\tsecond\t2.0\n" +
			"120000\tthird\t3.0\n"
		)
	}

	void "WriteToCsv, without timestamps included, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
				includeTimestamps: [value: false]
			]
		])

		then:
		testForFileContent(
			"firstInput\tsecondInput\n" +
			"first\t1.0\n" +
			"second\t2.0\n" +
			"third\t3.0\n"
		)
	}

	void "WriteToCsv, with custom delimiter, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
			    separator: [value: "|"]
			]
		])

		then:
		testForFileContent(
			"timestamp|firstInput|secondInput\n" +
				"0|first|1.0\n" +
				"60000|second|2.0\n" +
				"120000|third|3.0\n"
		)
	}

	void "WriteToCsv, with quoteColumns enabled, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
			    quoteColumns: [value: true]
			]
		])

		then:
		testForFileContent(
			'"timestamp"\t"firstInput"\t"secondInput"\n' +
				'"0"\t"first"\t"1.0"\n' +
				'"60000"\t"second"\t"2.0"\n' +
				'"120000"\t"third"\t"3.0"\n'
		)
	}

	void "WriteToCsv, with other timeformat, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
			    timeFormat: [value: "ISO_8601_UTC"]
			]
		])

		then:
		testForFileContent(
			"timestamp\tfirstInput\tsecondInput\n" +
				"1970-01-01T00:00:00.000Z\tfirst\t1.0\n" +
				"1970-01-01T00:01:00.000Z\tsecond\t2.0\n" +
				"1970-01-01T00:02:00.000Z\tthird\t3.0\n"
		)
	}
}
