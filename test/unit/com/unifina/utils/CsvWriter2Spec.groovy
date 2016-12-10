package com.unifina.utils

import spock.lang.Specification

class CsvWriter2Spec extends Specification {

	def stringWriter = new StringWriter()

	def "writeRow can write csv lines"() {
		def csvWriter = new CSVWriter2(stringWriter, ",", false)

		when:
		csvWriter.writeRow(["hello", 3.141592, "world"])
		csvWriter.writeRow(["aaa", 6, "bbb"])
		csvWriter.close()

		then:
		stringWriter.toString() == "hello,3.141592,world\naaa,6,bbb\n"
	}

	def "writeRow can write csv lines with columns in quotes"() {
		def csvWriter = new CSVWriter2(stringWriter, ",", true)

		when:
		csvWriter.writeRow(["hello", 3.141592, "world"])
		csvWriter.writeRow(["aaa", 6, "bbb"])
		csvWriter.close()

		then:
		stringWriter.toString() == '"hello","3.141592","world"\n"aaa","6","bbb"\n'
	}

	def "writeRow, in quote column mode, replaces double quotes with two double quotes"() {
		def csvWriter = new CSVWriter2(stringWriter, ",", true)

		when:
		csvWriter.writeRow(["\"ello", "worl\"\""])
		csvWriter.close()

		then:
		stringWriter.toString() == '"""ello","worl"""""\n'
	}

	def "writeRow throws exception if writing wrong number of rows"() {
		def csvWriter = new CSVWriter2(stringWriter, ",", false)
		csvWriter.writeRow(["hello", 3.141592, "world"])

		when:
		csvWriter.writeRow(["aaa"])

		then:
		def e = thrown(RuntimeException)
		e.message == "Expected 3 columns but was 1 for row: [aaa]"
	}

	def "writeRow counts number of row written"() {
		when:
		def csvWriter = new CSVWriter2(stringWriter, ",", false)
		then:
		csvWriter.numOfRowsWritten == 0

		when:
		csvWriter.writeRow([])
		csvWriter.writeRow([])
		csvWriter.writeRow([])
		then:
		csvWriter.numOfRowsWritten == 3
	}
}
