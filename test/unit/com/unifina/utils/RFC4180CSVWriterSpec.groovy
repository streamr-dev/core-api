package com.unifina.utils

import spock.lang.Specification

class RFC4180CSVWriterSpec extends Specification {

	def stringWriter = new StringWriter()
	def csvWriter = new RFC4180CSVWriter(stringWriter)

	def "writeRow can write csv lines"() {
		when:
		csvWriter.writeRow(["hello", 3.141592, "world"])
		csvWriter.writeRow(["aaa", 6, "bbb"])
		csvWriter.writeRow(["tri\"\"cky", "\n", ",,,"])
		csvWriter.close()

		then:
		stringWriter.toString() == "hello,3.141592,world" + "\r\n" +
			"aaa,6,bbb" + "\r\n" +
			'"tri""""cky","\n",",,,"' + "\r\n"
	}

	def "writeRow throws exception if writing wrong number of rows"() {
		csvWriter.writeRow(["hello", 3.141592, "world"])

		when:
		csvWriter.writeRow(["aaa"])

		then:
		def e = thrown(RuntimeException)
		e.message == "Expected 3 columns but was 1 for row: [aaa]"
	}

	def "writeRow counts number of row written"() {
		expect:
		csvWriter.numOfRowsWritten == 0

		when:
		csvWriter.writeRow(["a"])
		csvWriter.writeRow(["b"])
		csvWriter.writeRow(["c"])
		then:
		csvWriter.numOfRowsWritten == 3
	}
}
