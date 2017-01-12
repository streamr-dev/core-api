package com.unifina.signalpath.utils

import com.unifina.datasource.DataSource
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeExportCSVContext
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class ExportCSVSpec extends Specification {

	FakeExportCSVContext fakeFileHolder = new FakeExportCSVContext()
	ExportCSV module

	def setup() {
		module = new ExportCSV(fakeFileHolder)
		module.init()
		module.getInput("input-1")
		module.getInput("input-2")
		module.getInput("input-3")
		module.getInput("input-1").setDisplayName("firstInput")
		module.getInput("input-2").setDisplayName("secondInput")
	}

	private boolean testForFileContentAndUiMessages(String s, Map channelMessages) {
		Map inputValues = [
			firstInput : ["first", "second", "third"],
			secondInput: [1, 2, 3]*.doubleValue(),
			in3        : [true, false, false]
		]
		Map outputValues = [:]

		ModuleTestHelper moduleTestHelper
		def builder = new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(60 * 1000)
			.uiChannelMessages(channelMessages)
			// Don't test deserialization, since resuming to write the same csv file will not be possible
			.serializationModes(new HashSet<>([ModuleTestHelper.SerializationMode.NONE, ModuleTestHelper.SerializationMode.SERIALIZE]))
			.overrideGlobals { Globals globals ->
				globals.setUserTimeZone(TimeZone.getTimeZone("EST"))
				return globals
			}
			.afterEachTestCase {
				String fileContents = fakeFileHolder.resolveStringsAndReset()
				assert fileContents == s
			}
		moduleTestHelper = builder.build()
		return moduleTestHelper.test()
	}

	void "ExportCSV works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,firstInput,secondInput" + "\r\n" +
				"1970-01-01T00:00:00.000Z,first,1.0" + "\r\n" +
				"1970-01-01T00:01:00.000Z,second,2.0" + "\r\n" +
				"1970-01-01T00:02:00.000Z,third,3.0" + "\r\n",
			[uiChannelId: [
					[type: "csvUpdate", rows: 2l, kilobytes: 666l],
					[type: "csvUpdate", rows: 4l, kilobytes: 666l],
					[type: "csvUpdate", rows: 4l, kilobytes: 666l, file: "test.csv"]
			]]
		)
	}

	void "ExportCSV, with header writing disabled, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
				writeHeader: [value: false]
			]
		])

		then:
		testForFileContentAndUiMessages(
				"1970-01-01T00:00:00.000Z,first,1.0" + "\r\n" +
				"1970-01-01T00:01:00.000Z,second,2.0" + "\r\n" +
				"1970-01-01T00:02:00.000Z,third,3.0" + "\r\n",
			[uiChannelId: [
				[type: "csvUpdate", rows: 1l, kilobytes: 666l],
				[type: "csvUpdate", rows: 3l, kilobytes: 666l],
				[type: "csvUpdate", rows: 3l, kilobytes: 666l, file: "test.csv"]
			]]
		)
	}

	void "ExportCSV, without timestamps included, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
				includeTimestamps: [value: false]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"firstInput,secondInput" + "\r\n" +
				"first,1.0" + "\r\n" +
				"second,2.0" + "\r\n" +
				"third,3.0" + "\r\n",
			[uiChannelId: [
				[type: "csvUpdate", rows: 2l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l, file: "test.csv"]
			]]
		)
	}

	void "ExportCSV, with milliseconds since epoch timeformat, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
			    timeFormat: [value: "MILLISECONDS_SINCE_EPOCH"]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,firstInput,secondInput" + "\r\n" +
				"0,first,1.0" + "\r\n" +
				"60000,second,2.0" + "\r\n" +
				"120000,third,3.0" + "\r\n",
			[uiChannelId: [
				[type: "csvUpdate", rows: 2l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l, file: "test.csv"]
			]]
		)
	}

	void "ExportCSV, with user's timezone ISO8601 timeformat, works as expected"() {
		when:
		module.configure([
			uiChannel: [id: "uiChannelId"],
			options: [
				timeFormat: [value: "ISO_8601_LOCAL"]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,firstInput,secondInput" + "\r\n" +
				"1969-12-31T19:00:00.000-0500,first,1.0" + "\r\n" +
				"1969-12-31T19:01:00.000-0500,second,2.0" + "\r\n" +
				"1969-12-31T19:02:00.000-0500,third,3.0" + "\r\n",
			[uiChannelId: [
				[type: "csvUpdate", rows: 2l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l],
				[type: "csvUpdate", rows: 4l, kilobytes: 666l, file: "test.csv"]
			]]
		)
	}
}
