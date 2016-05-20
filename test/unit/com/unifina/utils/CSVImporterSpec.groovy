package com.unifina.utils

import java.nio.file.Paths

import spock.lang.Specification


class CSVImporterSpec extends Specification {

	def setup() {

	}

	def cleanup() {

	}

	void "test detecting a schema from a csv file"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/test-upload-file.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()

		then:
		schema.entries.length == 5
		schema.timestampColumnIndex == 0
		schema.entries[0].type == "timestamp"
		schema.entries[1].name == "price"
		schema.entries[1].type == "number"
		schema.entries[2].name == "size"
		schema.entries[2].type == "number"
		schema.entries[3].name == "really"
		schema.entries[3].type == "boolean"
		schema.entries[4].name == "comment"
		schema.entries[4].type == "string"
	}

	void "test reading values from a csv file"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/test-upload-file.csv").toURI()).toFile()
		int rowsRead = 0
		List<CSVImporter.LineValues> firstRows = []

		when:
		CSVImporter csv = new CSVImporter(file)
		for (CSVImporter.LineValues line : csv) {
			if (line == null)
				continue

			if (rowsRead < 25)
				firstRows << line

			rowsRead++
		}

		then:
		rowsRead == 735
		firstRows[0].values[0] instanceof Date
		firstRows[0].values[1] instanceof Double
		firstRows[0].values[1] == 477.74
		firstRows[0].values[2] instanceof Double
		firstRows[0].values[2] == 100.0
		firstRows[0].values[3] instanceof Boolean
		firstRows[0].values[3] == true
		firstRows[0].values[4] == null
		firstRows[23].values[1] == 477.16
		firstRows[23].values[4] == "No way!"
	}

	void "test detecting the schema from another csv file"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/crime-records.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file, null, 0, "MM/dd/yy HH:mm")
		CSVImporter.Schema schema = csv.getSchema()

		then:
		schema.entries.length == 9
		schema.timestampColumnIndex == 0
		schema.entries[0].type == "timestamp"
		schema.entries[1].name == "address"
		schema.entries[1].type == "string"
		schema.entries[2].name == "district"
		schema.entries[2].type == "number"
		schema.entries[3].name == "beat"
		schema.entries[3].type == "string"
		schema.entries[4].name == "grid"
		schema.entries[4].type == "number"
		schema.entries[5].name == "crimedescr"
		schema.entries[5].type == "string"
		schema.entries[6].name == "ucr_ncic_code"
		schema.entries[6].type == "number"
		schema.entries[7].name == "latitude"
		schema.entries[7].type == "number"
		schema.entries[8].name == "longitude"
		schema.entries[8].type == "number"
	}

	void "test detecting the timestamp from epoch"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/epoch-test.csv").toURI()).toFile()
		CSVImporter csv = new CSVImporter(file, null, 0, "unix")
		CSVImporter.Schema schema = csv.getSchema()

		int rowsRead = 0
		List<CSVImporter.LineValues> firstRows = []

		expect:
		schema.entries.length == 6
		schema.timestampColumnIndex == 0

		when:
		for (CSVImporter.LineValues line : csv) {
			if (line == null)
				continue

			if (rowsRead < 25)
				firstRows << line

			rowsRead++
		}

		then:
		rowsRead == 8
		firstRows[0].values[0] instanceof Date
		Date d = firstRows[5].values[0]
		d instanceof Date
		firstRows[5].values[2] == 53
	}
	
	
	
	void "test detecting timestamp from another time format"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/real_time_sales.csv").toURI()).toFile()
		
		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()
		
		then:
		schema.entries.length == 12
		schema.timestampColumnIndex == 5
	}

	void "should fail when the order of the lines is not chronological"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/invalid-chronological-order.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file)
		// Tries to parse each line
		for (CSVImporter.LineValues line : csv) {}

		then:
		thrown RuntimeException
	}

	void "test giving the field type in a map"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/test-upload-file.csv").toURI()).toFile()
		List fields = [
		        [name: "really", type: "string"],
				[name: "price", type: "string"]
		]

		when: "the fields are not given"
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()

		then: "the field types are autodetected"
		schema.entries[1].type == "number"
		schema.entries[2].type == "number"
		schema.entries[3].type == "boolean"

		when: "the fields are given"
		csv = new CSVImporter(file, fields)
		schema = csv.getSchema()

		then: "the field types are all strings as given"
		schema.entries[1].type == "string"
		schema.entries[2].type == "number"
		schema.entries[3].type == "string"
	}

	void "can detect multiple date fields"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/two-date-fields.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()

		then:
		schema.entries.length == 3
		schema.timestampColumnIndex == 0
		schema.entries[0].type == "timestamp"
		schema.entries[1].type == "number"
		schema.entries[2].type == "timestamp"
	}

	void "should not fail with invalid number of columns on row"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/invalid-number-of-columns.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()

		then: "schema contains all entries, but two of them are null (undetected)"
		schema.entries.length == 21
		schema.entries.findAll {it == null}.size() == 2
	}

	void "should ignore empty string fields by default"() {
		def value
		setup:
		File file = Paths.get(getClass().getResource("test-files/empty-strings.csv").toURI()).toFile()

		when: "lines are read in"
			CSVImporter csv = new CSVImporter(file)
			CSVImporter.Schema schema = csv.getSchema()
			int i = 0
			for (CSVImporter.LineValues line : csv) {
				if(i == 5) {
					value = line.values[4]
					break
				}
				i++
			}

		then: "value is null"
		value == null
	}

	void "should read empty strings if ignoreEmptyFields == false"() {
		def value
		setup:
			File file = Paths.get(getClass().getResource("test-files/empty-strings.csv").toURI()).toFile()

		when: "lines are read in"
			CSVImporter csv = new CSVImporter(file, null, null, null, false)
			CSVImporter.Schema schema = csv.getSchema()
			int i = 0
			for (CSVImporter.LineValues line : csv) {
				if (
				i == 5) {
					value = line.values[4]
					break
				}
				i++
			}

		then: "value is an empty string"
			value == ""
	}

	void "should not fail if the file has empty rows"() {
		setup:
		File file = Paths.get(getClass().getResource("test-files/with-empty-rows.csv").toURI()).toFile()

		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()
		for (CSVImporter.LineValues line : csv) {}

		then: "the rows have been read"
		schema.entries.length == 5
	}
	
}