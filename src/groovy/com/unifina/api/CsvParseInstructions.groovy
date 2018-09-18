package com.unifina.api

import grails.validation.Validateable

@Validateable
class CsvParseInstructions {
	String fileId
	Integer timestampColumnIndex
	String dateFormat

	static constraints = {
		timestampColumnIndex(min: 0)
	}
}
