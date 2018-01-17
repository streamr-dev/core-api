package com.unifina.signalpath.utils

import com.unifina.UiChannelMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.FakeExportCSVContext
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.support.GrailsUnitTestMixin

@Mixin(GrailsUnitTestMixin)
class ExportCSVSpec extends UiChannelMockingSpecification {

	FakeExportCSVContext fakeFileHolder = new FakeExportCSVContext()
	ExportCSV module

	def setup() {
		mockServicesForUiChannels()
		module = new ExportCSV(fakeFileHolder)
		// For variadics, ensure inputs before configuration!
		module.getInput("generated-input-1").setDisplayName("in1")
		module.getInput("generated-input-2").setDisplayName("in2")
		module.getInput("generated-input-3").setDisplayName("in3")
	}

	private boolean testForFileContentAndUiMessages(String s, Map channelMessages) {
		testForFileContentAndUiMessages([
				in1: ["first", "second", "third"],
				in2: [1, 2, 3]*.doubleValue(),
				in3: [true, false, false]
		], s, channelMessages)
	}

	private boolean testForFileContentAndUiMessages(Map inputValues, String s, Map channelMessages) {
		Map outputValues = [:]

		ModuleTestHelper moduleTestHelper
		def builder = new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.timeToFurtherPerIteration(60 * 1000)
			.uiChannelMessages(channelMessages, getSentMessagesByStreamId())
			// Don't test deserialization, since resuming to write the same csv file will not be possible
			.serializationModes(new HashSet<>([ModuleTestHelper.SerializationMode.NONE, ModuleTestHelper.SerializationMode.CLEAR, ModuleTestHelper.SerializationMode.SERIALIZE]))
			.overrideGlobals { Globals globals ->
				globals.setUserTimeZone(TimeZone.getTimeZone("EST"))
				return globals
			}
			.afterEachTestCase {
				if (s) {
					String fileContents = fakeFileHolder.resolveStringsAndReset()
					assert fileContents == s
				} else {
					assert !fakeFileHolder.isFileOpened()
				}
			}
		moduleTestHelper = builder.build()
		return moduleTestHelper.test()
	}

	void "ExportCSV works as expected"() {
		when:
		setupModule(module, [
			uiChannel: [id: "uiChannelId"]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,in1,in2" + "\r\n" +
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

	void "ExportCSV reports 0 rows and 0 kB if nothing is written"() {
		when:
		setupModule(module, [
				uiChannel: [id: "uiChannelId"]
		])

		then:
		testForFileContentAndUiMessages(
				[
						in1: [],
						in2: [],
						in3: []
				],
				null, // no file content
				[uiChannelId: [
						[type: "csvUpdate", rows: 0l, kilobytes: 0l],
				]]
		)
	}

	void "ExportCSV, with header writing disabled, works as expected"() {
		when:
		setupModule(module, [
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
		setupModule(module, [
			uiChannel: [id: "uiChannelId"],
			options: [
				includeTimestamps: [value: false]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"in1,in2" + "\r\n" +
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
		setupModule(module, [
			uiChannel: [id: "uiChannelId"],
			options: [
			    timeFormat: [value: "MILLISECONDS_SINCE_EPOCH"]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,in1,in2" + "\r\n" +
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
		setupModule(module, [
			uiChannel: [id: "uiChannelId"],
			options: [
				timeFormat: [value: "ISO_8601_LOCAL"]
			]
		])

		then:
		testForFileContentAndUiMessages(
			"timestamp,in1,in2" + "\r\n" +
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
