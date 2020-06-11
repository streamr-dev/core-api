package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class CsvParseInstructions {
	String fileId
	Integer timestampColumnIndex
	String dateFormat

	static constraints = {
		timestampColumnIndex(min: 0)
	}
}
