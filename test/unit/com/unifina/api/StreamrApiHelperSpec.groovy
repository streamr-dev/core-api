package com.unifina.api

import grails.orm.HibernateCriteriaBuilder
import spock.lang.Specification

class StreamrApiHelperSpec extends Specification {
	
	HibernateCriteriaBuilder builder
	Map params

	void setup() {
		builder = Mock(HibernateCriteriaBuilder)
		params = [:]
	}

	void "createListCriteria() with empty params"() {
		when:
		def c = StreamrApiHelper.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		0 * builder._
	}

	void "createListCriteria() with search"() {
		when:
		params.search = "foo"
		def c = StreamrApiHelper.createListCriteria(params, ["name", "description"], {})
		c.delegate = builder
		c()

		then:
		1 * builder.or(_) >> {Closure orParam ->
			orParam.delegate = builder
			orParam()
			return null
		}
		and:
		1 * builder.like("name", "%foo%")
		1 * builder.like("description", "%foo%")
		0 * builder._
	}

	void "createListCriteria() with sort"() {
		when:
		params.sort = "name"
		def c = StreamrApiHelper.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.order("name", "asc")
		0 * builder._
	}

	void "createListCriteria() with sort desc"() {
		when:
		params.sort = "name"
		params.order = "desc"
		def c = StreamrApiHelper.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.order("name", "desc")
		0 * builder._
	}

	void "createListCriteria() with max"() {
		when:
		params.max = "5"
		def c = StreamrApiHelper.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.invokeMethod('maxResults', [5]) // dynamic invoke for some groovy reason
		0 * builder._
	}

	void "createListCriteria() with offset"() {
		when:
		params.offset = "10"
		def c = StreamrApiHelper.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.invokeMethod('firstResult', [10]) // dynamic invoke for some groovy reason
		0 * builder._
	}

	void "createListCriteria() with additional custom criteria"() {
		when:
		params.max = "10"
		def c = StreamrApiHelper.createListCriteria(params, [], {
			eq("foo", "bar")
		})
		c.delegate = builder
		c()

		then:
		1 * builder.eq("foo", "bar")
		1 * builder.invokeMethod('maxResults', [10]) // dynamic invoke for some groovy reason
		0 * builder._
	}

	void "createListCriteria() with various options"() {
		when:
		params.search = "foo"
		params.sort = "name"
		params.order = "desc"
		params.max = "5"
		params.offset = "10"
		def c = StreamrApiHelper.createListCriteria(params, ["name", "desc"], {})
		c.delegate = builder
		c()

		then:
		1 * builder.or(_) >> {Closure orParam ->
			orParam.delegate = builder
			orParam()
			return null
		}
		1 * builder.order("name", "desc")
		1 * builder.invokeMethod('maxResults', [5]) // dynamic invoke for some groovy reason
		1 * builder.invokeMethod('firstResult', [10]) // dynamic invoke for some groovy reason

		and:
		1 * builder.like("name", "%foo%")
		1 * builder.like("desc", "%foo%")
		0 * builder._
	}

	void "isPublicFlagOn()"() {
		expect:
		!StreamrApiHelper.isPublicFlagOn([:])
		StreamrApiHelper.isPublicFlagOn([public: "true"])
		StreamrApiHelper.isPublicFlagOn([public: "TRUE"])
		!StreamrApiHelper.isPublicFlagOn([public: "false"])
	}

}
