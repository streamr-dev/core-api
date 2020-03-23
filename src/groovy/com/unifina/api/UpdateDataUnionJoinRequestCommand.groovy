package com.unifina.api

import com.unifina.controller.api.DataUnionJoinRequestApiController
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class UpdateDataUnionJoinRequestCommand {
	String state
	static constraints = {
		state(nullable: false, validator: { String value ->
			return DataUnionJoinRequestApiController.isState(value) != null
		})
	}
}
