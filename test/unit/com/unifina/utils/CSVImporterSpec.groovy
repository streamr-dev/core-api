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
		File file = Paths.get(getClass().getResource("test-upload-file.csv").toURI()).toFile()
		
		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()
		
		then:
		schema.entries.length == 5
		schema.timestampColumnIndex == 0
		schema.entries[0].name == "timestamp"
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
		File file = Paths.get(getClass().getResource("test-upload-file.csv").toURI()).toFile()
		int rowsRead = 0
		List<CSVImporter.LineValues> firstRows = []
		
		when:
		CSVImporter csv = new CSVImporter(file)
		for (CSVImporter.LineValues line : csv) {
			if (line==null)
				continue
			
			if (rowsRead<25)
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
	
}
