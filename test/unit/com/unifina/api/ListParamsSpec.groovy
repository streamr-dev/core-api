package com.unifina.api

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
class ListParamsSpec extends Specification {

	private static class TestListParams extends ListParams {

		@Override
		protected String getSearchFields() {
			return []
		}

		@Override
		protected Closure additionalCriteria() {
			return {}
		}
	}

	void "default values"() {
		def params = new TestListParams()
		expect:
		params.toMap() == [
			search: null,
			sortBy: null,
			order: "asc",
			max: 100,
			offset: null,
			publicAccess: false
		]
	}

	void "default values pass validation"() {
		def params = new TestListParams()
		expect:
		params.validate()
	}

	void "takes in values via constructor"() {
		def params = new TestListParams(
			search: "cool blockchain canvas",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			publicAccess: true
		)

		expect:
		params.toMap() == [
			search: "cool blockchain canvas",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			publicAccess: true
		]
	}

	void "reasonable values pass validation"() {
		def params = new TestListParams(
			search: "cool blockchain canvas",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			publicAccess: true
		)

		expect:
		params.validate()
	}

	@Unroll
	void "#map does not pass validation"(Map map, int numOfErrors, List<String> fieldsWithError) {
		def params = new TestListParams(map)

		expect:
		!params.validate()
		params.errors.errorCount == numOfErrors
		params.errors.getFieldErrors()*.field == fieldsWithError

		where:
		map                  | numOfErrors | fieldsWithError
		[search: ""]         | 1           | ["search"]
		[sortBy: ""]         | 1           | ["sortBy"]
		[order: "chaos"]     | 1           | ["order"]
		[max: 0]             | 1           | ["max"]
		[max: 101]           | 1           | ["max"]
		[offset: -1]         | 1           | ["offset"]
	}
}
