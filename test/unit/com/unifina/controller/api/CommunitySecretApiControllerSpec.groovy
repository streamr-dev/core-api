package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.BadRequestException
import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.CommunitySecretService
import com.unifina.service.CommunityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunitySecretApiController)
@Mock([UnifinaCoreAPIFilters, CommunitySecret, SecUser])
class CommunitySecretApiControllerSpec extends Specification {
	SecUser me
	final String communityAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String validID = "L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g"

	void setup() {
		me = new SecUser(id: 1, name: "firstname lastname", username: "firstname.lastname@address.com", password: "salasana")
		me.save(validate: true, failOnError: true)
		CommunitySecret secret = new CommunitySecret(
			name: "name of the community secret",
			secret: "secret",
			communityAddress: communityAddress,
		)
		secret.id = "secret-id"
		secret.save(validate: true, failOnError: true)

		controller.communitySecretService = Mock(CommunitySecretService)
		controller.communityService = Mock(CommunityService)
	}

	void "findAll() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret 1",
			communityAddress: communityAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		CommunitySecret s2 = new CommunitySecret(
			name: "secret 2",
			secret: "secret 2",
			communityAddress: communityAddress,
		)
		s2.id = "2"
		s2.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		withFilters(action: "findAll") {
			controller.findAll()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.findAll(communityAddress) >> [s1, s2]
		response.json[0].id == "1"
		response.json[0].name == "secret 1"
		response.json[0].communityAddress == communityAddress
		response.json[1].id == "2"
		response.json[1].name == "secret 2"
		response.json[1].communityAddress == communityAddress
	}

	void "findAll() bad request on invalid community address"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "findAll") {
			controller.findAll()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findAll() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		withFilters(action: "findAll") {
			controller.findAll()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "ACCESS_DENIED"
	}

	void "create() test"() {
		CommunitySecret secret = new CommunitySecret(
			name: "secret name",
			secret: "secret",
			communityAddress: communityAddress,
		)
		secret.id = "1"
		secret.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "POST"
		request.json = [
			name: "secret name",
			secret: "secret",
		]
		params.communityAddress = communityAddress
		withFilters(action: "create") {
			controller.create()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.create(communityAddress, _ as CommunitySecretCommand) >> secret
		response.json.id == "1"
		response.json.name == "secret name"
		response.json.communityAddress == communityAddress
	}

	void "create() bad request on invalid community address"() {
		when:
		request.method = "POST"
		request.json = [
			name: "secret name",
			secret: "secret",
		]
		params.communityAddress = "0x123"
		withFilters(action: "create") {
			controller.create()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "create() bad request on invalid community name"() {
		when:
		request.method = "POST"
		request.json = [
			name: "",
		]
		params.communityAddress = communityAddress
		withFilters(action: "create") {
			controller.create()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "create() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "POST"
		request.json = [
			name: "community name",
		]
		params.communityAddress = communityAddress
		withFilters(action: "create") {
			controller.create()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "ACCESS_DENIED"
	}

	void "find() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret 1",
			communityAddress: communityAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		withFilters(action: "find") {
			controller.find()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.find(communityAddress, validID) >> s1
		response.json.id == "1"
		response.json.name == "secret 1"
		response.json.communityAddress == communityAddress
	}

	void "find() bad request on invalid community address"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.communitySecretId = validID
		withFilters(action: "find") {
			controller.find()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "find() bad request on invalid community secret id"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.communitySecretId = null
		withFilters(action: "find") {
			controller.find()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "find() not found when community secret not found by id"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.communitySecretId = validID // ID not found in DB
		withFilters(action: "find") {
			controller.find()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.find(communityAddress, validID) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "find() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		withFilters(action: "find") {
			controller.find()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "ACCESS_DENIED"
	}

	void "update() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "name",
			secret: "secret 1",
			communityAddress: communityAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "PUT"
		params.communityAddress = communityAddress
		params.communitySecretId = validID // ID not found in DB
		request.json = [
			name: "new name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.update(communityAddress, validID, _ as CommunitySecretCommand) >> {
			s1.name = "new name"
			return s1
		}
		response.status == 200
		response.json.id  == "1"
		response.json.name == "new name"
	}

	void "update() bad request on invalid community address"() {
		when:
		request.method = "PUT"
		params.communityAddress = "0x123"
		params.communitySecretId = "123"
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid community secret id"() {
		when:
		request.method = "PUT"
		params.communityAddress = communityAddress
		params.communitySecretId = null
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid community secret name"() {
		when:
		request.method = "PUT"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		request.json = [
			name: "",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() not found on when community secret not found by id"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		params.communityAddress = communityAddress
		params.communitySecretId = validID // ID not found in DB
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.update(communityAddress, validID, _ as CommunitySecretCommand) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "update() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "ACCESS_DENIED"
	}

	void "delete() test"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.delete(communityAddress, validID)
		response.status == 204
	}

	void "delete() bad request on invalid community address"() {
		when:
		request.method = "DELETE"
		params.communityAddress = "0x123"
		params.communitySecretId = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() bad request on invalid community secret id"() {
		when:
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.communitySecretId = null
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.communitySecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() not found on non-existing community secret id"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> true
		1 * controller.communitySecretService.delete(communityAddress, validID) >> {
			throw new NotFoundException("mocked: not found!")
		}
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "delete() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.communitySecretId = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.communityService.checkAdminAccessControl(me, communityAddress) >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "ACCESS_DENIED"
	}
}
