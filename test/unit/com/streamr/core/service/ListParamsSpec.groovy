package com.streamr.core.service

import com.streamr.core.domain.Product
import grails.orm.HibernateCriteriaBuilder
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.validation.Validateable
import spock.lang.Specification
import spock.lang.Unroll

@TestMixin(GrailsUnitTestMixin)
@Mock(Product)
class ListParamsSpec extends Specification {

	@Validateable
	private static class ExampleParams extends ListParams {
		String additional

		static constraints = {
			additional(blank: false, nullable: true)
		}

		@Override
		protected List<String> getSearchFields() {
			return ["name", "description"]
		}

		@Override
		protected Closure additionalCriteria() {
			return {
				if (additional != null) {
					eq("additional", additional)
				}
			}
		}
	}

	void "default values"() {
		def params = new ExampleParams()
		expect:
		params.toMap() == [
			search: null,
			sortBy: null,
			order: null,
			max: 1000,
			offset: 0,
			grantedAccess: true,
			publicAccess: false
		]
	}

	void "default values pass validation"() {
		def params = new ExampleParams()
		expect:
		params.validate()
	}

	void "takes in values via constructor"() {
		def params = new ExampleParams(
			search: "cool blockchain stream",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			grantedAccess: false,
			publicAccess: true
		)

		expect:
		params.toMap() == [
			search: "cool blockchain stream",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			grantedAccess: false,
			publicAccess: true
		]
	}

	void "reasonable values pass validation"() {
		def params = new ExampleParams(
			search: "cool blockchain stream",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			grantedAccess: false,
			publicAccess: true
		)

		expect:
		params.validate()
	}

	@Unroll
	void "#map does not pass validation"(Map map, int numOfErrors, List<String> fieldsWithError) {
		def params = new ExampleParams(map)

		expect:
		!params.validate()
		params.errors.errorCount == numOfErrors
		params.errors.getFieldErrors()*.field == fieldsWithError

		where:
		map              | numOfErrors | fieldsWithError
		[search: ""]     | 1           | ["search"]
		[sortBy: ""]     | 1           | ["sortBy"]
		[order: "chaos"] | 1           | ["order"]
		[max: 0]         | 1           | ["max"]
		[max: 1001]      | 1           | ["max"]
		[offset: -1]     | 1           | ["offset"]
		[additional: ""] | 1           | ["additional"]
	}


	void "createListCriteria() with few values creates expected criteria"() {
		def builder = Mock(HibernateCriteriaBuilder)

		when:
		def criteria = new ExampleParams().createListCriteria()
		criteria.delegate = builder
		criteria()

		then:
		1 * builder.invokeMethod("firstResult", [0])
		1 * builder.invokeMethod("maxResults", [1000])
		0 * builder._
	}

	void "createListCriteria() with reasonable values creates expected criteria"() {
		def builder = Mock(HibernateCriteriaBuilder)
		def orBuilder = Mock(HibernateCriteriaBuilder)

		when:
		def criteria = new ExampleParams(
			search: "cool blockchain stream",
			sortBy: "createdAt",
			order: "desc",
			max: 50,
			offset: 1337,
			grantedAccess: false,
			publicAccess: true,
			additional: "additional information here"
		).createListCriteria()
		criteria.delegate = builder
		criteria()

		then:
		1 * builder.or(_) >> { Closure orParam ->
			orParam.delegate = orBuilder
			orParam()
			return null
		}
		1 * builder.order("id", "asc")
		1 * builder.order('createdAt', 'desc')
		1 * builder.invokeMethod("maxResults", [50])
		1 * builder.invokeMethod("firstResult", [1337])
		1 * builder.eq('additional', "additional information here")
		0 * builder._

		and:
		1 * orBuilder.like("name", "%cool blockchain stream%")
		1 * orBuilder.like("description", "%cool blockchain stream%")
		0 * orBuilder._
	}

	void "createListCriteria() supports fetching by offsets"() {
		(1..100).each {
			Product p = new Product(name: "Product ${it}")
			p.id = "${it}"
			p.save(validate: false, failOnError: true)
		}

		def p1 = new ProductListParams(max: 30)
		def p2 = new ProductListParams(max: 15, offset: 15)
		def p3 = new ProductListParams(max: 30, offset: 30)
		def p4 = new ProductListParams(max: 30, offset: 60)
		def p5 = new ProductListParams(max: 30, offset: 90)
		def p6 = new ProductListParams(max: 30, offset: 1100)

		expect:
		Product.withCriteria(p1.createListCriteria())*.id == (1..30)*.toString()
		Product.withCriteria(p2.createListCriteria())*.id == (16..30)*.toString()
		Product.withCriteria(p3.createListCriteria())*.id == (31..60)*.toString()
		Product.withCriteria(p4.createListCriteria())*.id == (61..90)*.toString()
		Product.withCriteria(p5.createListCriteria())*.id == (91..100)*.toString()
		Product.withCriteria(p6.createListCriteria()).empty
	}

	void "CORE-1346 fix duplicate products in paging"() {
		def builder = Mock(HibernateCriteriaBuilder)

		when:
		def criteria = new ExampleParams(sortBy: "name").createListCriteria()
		criteria.delegate = builder
		criteria()

		then:
		1 * builder.order("id", "asc")
		1 * builder.order("name", "asc")
		1 * builder.invokeMethod("firstResult", [0])
		1 * builder.invokeMethod("maxResults", [1000])
		0 * builder._
	}

	void "CORE-1346 fix duplicate products in paging. no double order by id"() {
		def builder = Mock(HibernateCriteriaBuilder)

		when:
		def criteria = new ExampleParams(sortBy: "id").createListCriteria()
		criteria.delegate = builder
		criteria()

		then:
		1 * builder.order("id", "asc")
		1 * builder.invokeMethod("firstResult", [0])
		1 * builder.invokeMethod("maxResults", [1000])
		0 * builder._
	}
}
