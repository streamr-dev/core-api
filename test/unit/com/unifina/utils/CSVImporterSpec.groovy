package com.unifina.utils

import java.nio.file.Paths

import spock.lang.Specification


class CSVImporterSpec extends Specification {

	CSVImporter.Schema schema
	List<CSVImporter.LineValues> lines = []


	void readFile(String fileName, List<Map> fields = null, Integer timestampIndex = null, String format = null, String timeZone = null, boolean ignoreEmptyFields = true) {
		File file = Paths.get(getClass().getResource(fileName).toURI()).toFile()
		CSVImporter csv = new CSVImporter(file, fields, timestampIndex, format, timeZone, ignoreEmptyFields)
		schema = csv.getSchema()
		for (CSVImporter.LineValues lv : csv) {
			lines.add(lv);
		}
	}

	def setup() {
		schema = null
		lines.clear()
	}

	def cleanup() {

	}

	void "test detecting a schema from a csv file"() {
		when:
		readFile("test-files/test-upload-file.csv")

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
		when:
		readFile("test-files/test-upload-file.csv")

		then:
		lines.size() == 735
		lines.get(0).values[0] instanceof Date
		lines.get(0).values[1] instanceof Double
		lines.get(0).values[1] == 477.74
		lines.get(0).values[2] instanceof Double
		lines.get(0).values[2] == 100.0
		lines.get(0).values[3] instanceof Boolean
		lines.get(0).values[3] == true
		lines.get(0).values[4] == null
		lines.get(23).values[1] == 477.16
		lines.get(23).values[4] == "No way!"
	}

	void "test detecting the schema from another csv file"() {
		when:
		readFile("test-files/crime-records.csv", null, 0, "MM/dd/yy HH:mm")

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
		when:
		readFile("test-files/epoch-test.csv", null, 0, "unix")

		then:
		schema.entries.length == 6
		schema.timestampColumnIndex == 0
		lines.size() == 8
		lines.get(0).values[0] instanceof Date
		Date d = lines.get(5).values[0]
		d instanceof Date
		lines.get(5).values[2] == 53
	}
	
	
	
	void "test detecting timestamp from another time format"() {
		when:
		readFile("test-files/real_time_sales.csv")
		
		then:
		schema.entries.length == 12
		schema.timestampColumnIndex == 5
	}

	void "should fail when the order of the lines is not chronological"() {
		when:
		readFile("test-files/invalid-chronological-order.csv")

		then:
		Exception e = thrown()
		e.message.contains("chronological")
		e.message.contains("Line: 19")
	}

	void "test giving the field type in a map"() {
		String file = "test-files/test-upload-file.csv"
		List fields = [
		        [name: "really", type: "string"],
				[name: "price", type: "string"]
		]

		when: "the fields are not given"
		readFile(file)

		then: "the field types are autodetected"
		schema.entries[1].type == "number"
		schema.entries[2].type == "number"
		schema.entries[3].type == "boolean"

		when: "the fields are given"
		readFile(file, fields)

		then: "the field types are all strings as given"
		schema.entries[1].type == "string"
		schema.entries[2].type == "number"
		schema.entries[3].type == "string"
	}

	void "can detect multiple date fields"() {
		when:
		readFile("test-files/two-date-fields.csv")

		then:
		schema.entries.length == 3
		schema.timestampColumnIndex == 0
		schema.entries[0].type == "timestamp"
		schema.entries[1].type == "number"
		schema.entries[2].type == "timestamp"
	}

	void "should not fail with invalid number of columns on row"() {
		when:
		readFile("test-files/invalid-number-of-columns.csv")

		then: "schema contains all entries, but two of them are null (undetected)"
		schema.entries.length == 21
		schema.entries.findAll {it == null}.size() == 2
	}

	void "should ignore empty string fields by default"() {
		when: "lines are read in"
		readFile("test-files/empty-strings.csv")

		then: "value is null"
		lines.get(5).values[4] == null
	}

	void "should read empty strings if ignoreEmptyFields == false"() {
		when: "lines are read in"
		readFile("test-files/empty-strings.csv", null, null, null, null, false)

		then: "value is an empty string"
		lines.get(5).values[4] == ""
	}

	void "should not fail if the file has empty rows"() {
		when:
		readFile("test-files/with-empty-rows.csv")

		then: "the rows have been read"
		schema.entries.length == 5
	}

	void "should import the csv in the given timezone"() {
		when:
		// GMT+8
		readFile("test-files/test-upload-file.csv", null, null, null, "Asia/Hong_Kong")

		then: "date is correct"
		lines.get(0).values[0].toGMTString() == "23 Feb 2015 08:30:00 GMT"
	}

	void "if csv date has timezone itself, importer should use that"() {
		when:
		// GMT+8
		readFile("test-files/timezone-in-date.csv", null, null, null, "Asia/Hong_Kong")

		then: "dates are correct"
		lines.get(0).values[0].toGMTString() == "23 Feb 2015 16:30:00 GMT"
		lines.get(1).values[0].toGMTString() == "24 Feb 2015 02:30:00 GMT"
	}

	void "should accept timezone ids with custom format"() {
		when:
		Locale.setDefault(Locale.US);
		readFile("test-files/timezone-id.csv", null, null, "E MMM dd HH:mm:ss zzz yyyy")

		then: "Doesn't have any problem"
		schema.timestampColumnIndex == 1
	}
}
