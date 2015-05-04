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
	
	void "test detecting the schema from another csv file"(){
		setup:
		File file = Paths.get(getClass().getResource("test-files/crime-records.csv").toURI()).toFile()
		
		when:
		CSVImporter csv = new CSVImporter(file)
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
	
//	void "test detecting the timestamp from epoch"(){
//		setup:
//		File file = Paths.get(getClass().getResource("test-files/epoch-test.csv").toURI()).toFile()
//		
//		when:
//		CSVImporter csv = new CSVImporter(file)
//		CSVImporter.Schema schema = csv.getSchema()
//
//		then:
//		schema.entries.length == 6
//		schema.timestampColumnIndex == 0
//	}
	
	void "test detecting the schema from another csv file with tab separator"(){
		setup:
		File file = Paths.get(getClass().getResource("test-files/tab-test.csv").toURI()).toFile()
		
		when:
		CSVImporter csv = new CSVImporter(file)
		CSVImporter.Schema schema = csv.getSchema()

		then:
		schema.entries.length == 12
		schema.timestampColumnIndex == 8
		schema.entries[0].name == "street"
		schema.entries[0].type == "string"
		schema.entries[1].name == "city"
		schema.entries[1].type == "string"
		schema.entries[2].name == "zip"
		schema.entries[2].type == "number"
		schema.entries[3].name == "state"
		schema.entries[3].type == "string"
		schema.entries[4].name == "beds"
		schema.entries[4].type == "number"
		schema.entries[5].name == "baths"
		schema.entries[5].type == "number"
		schema.entries[6].name == "sq__ft"
		schema.entries[6].type == "number"
		schema.entries[7].name == "type"
		schema.entries[7].type == "string"
		schema.entries[8].name == "sale_date"
		schema.entries[8].type == "timestamp"
		schema.entries[9].name == "price"
		schema.entries[9].type == "number"
		schema.entries[10].name == "latitude"
		schema.entries[10].type == "number"
		schema.entries[11].name == "longitude"
		schema.entries[11].type == "number"
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
	
	
}
