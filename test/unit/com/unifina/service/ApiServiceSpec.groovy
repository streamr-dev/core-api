package com.unifina.service

import com.unifina.api.DashboardListParams
import com.unifina.api.ListParams
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.SecUser
import grails.orm.HibernateCriteriaBuilder
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ApiService)
@Mock([Dashboard, SecUser, PermissionService])
class ApiServiceSpec extends Specification {
	HibernateCriteriaBuilder builder
	Map params

	void setup() {
		builder = Mock(HibernateCriteriaBuilder)
		params = [:]
	}

	void "createListCriteria() with empty params"() {
		when:
		def c = service.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		0 * builder._
	}

	void "createListCriteria() with search"() {
		when:
		params.search = "foo"
		def c = service.createListCriteria(params, ["name", "description"], {})
		c.delegate = builder
		c()

		then:
		1 * builder.or(_) >> { Closure orParam ->
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
		def c = service.createListCriteria(params, [], {})
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
		def c = service.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.order("name", "desc")
		0 * builder._
	}

	void "createListCriteria() with max"() {
		when:
		params.max = "5"
		def c = service.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.invokeMethod('maxResults', [5]) // dynamic invoke for some groovy reason
		0 * builder._
	}

	void "createListCriteria() with offset"() {
		when:
		params.offset = "10"
		def c = service.createListCriteria(params, [], {})
		c.delegate = builder
		c()

		then:
		1 * builder.invokeMethod('firstResult', [10]) // dynamic invoke for some groovy reason
		0 * builder._
	}

	void "createListCriteria() with additional custom criteria"() {
		when:
		params.max = "10"
		def c = service.createListCriteria(params, [], {
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
		def c = service.createListCriteria(params, ["name", "desc"], {})
		c.delegate = builder
		c()

		then:
		1 * builder.or(_) >> { Closure orParam ->
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

	void "list() returns expected ListResult (1st page)"() {
		SecUser me = new SecUser(username: "me@me.com").save(failOnError: true, validate: false)
		setupDashboards(me)

		when:
		ListParams listParams = new DashboardListParams(max: 42)
		ListResult listResult = service.list(Dashboard, listParams, me)

		then:
		listResult.toMap().keySet() == ["totalCount", "numOfItems", "offset", "nextOffset", "items"] as Set
		listResult.toMap().subMap("totalCount", "numOfItems", "offset", "nextOffset") == [
			totalCount: 100,
			numOfItems: 42,
			offset: 0,
			nextOffset: 42
		]
		listResult.toMap().items*.name == (1..42).collect { "Dashboard ${it}"}
	}

	void "list() returns expected ListResult (2nd page)"() {
		SecUser me = new SecUser(username: "me@me.com").save(failOnError: true, validate: false)
		setupDashboards(me)

		when:
		ListParams listParams = new DashboardListParams(max: 42, offset: 42)
		ListResult listResult = service.list(Dashboard, listParams, me)

		then:
		listResult.toMap().keySet() == ["totalCount", "numOfItems", "offset", "nextOffset", "items"] as Set
		listResult.toMap().subMap("totalCount", "numOfItems", "offset", "nextOffset") == [
			totalCount: 100,
			numOfItems: 42,
			offset: 42,
			nextOffset: 84
		]
		listResult.toMap().items*.name == (43..84).collect { "Dashboard ${it}"}
	}

	void "list() returns expected ListResult (3rd page)"() {
		SecUser me = new SecUser(username: "me@me.com").save(failOnError: true, validate: false)
		setupDashboards(me)

		when:
		ListParams listParams = new DashboardListParams(max: 42, offset: 84)
		ListResult listResult = service.list(Dashboard, listParams, me)

		then:
		listResult.toMap().keySet() == ["totalCount", "numOfItems", "offset", "nextOffset", "items"] as Set
		listResult.toMap().subMap("totalCount", "numOfItems", "offset", "nextOffset") == [
			totalCount: 100,
			numOfItems: 16,
			offset: 84,
			nextOffset: null
		]
		listResult.toMap().items*.name == (85..100).collect { "Dashboard ${it}"}
	}

	void setupDashboards(SecUser me) {
		(1..100).each {
			def d = new Dashboard(name: "Dashboard ${it}").save(failOnError: true, validate: true)
			service.permissionService.systemGrantAll(me, d)
		}
	}
}
